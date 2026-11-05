package ma.fstg.security;

import ma.fstg.security.entities.User;
import ma.fstg.security.repositories.ProfilRepository;
import ma.fstg.security.repositories.RoleRepository;
import ma.fstg.security.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie le flux de l'étape 8 avec de vrais comptes en base (H2) et les vrais filtres.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthentificationJpaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private CommandLineRunner initialisation;

    private FormLoginRequestBuilder connexion(String nom, String motDePasse) {
        return formLogin("/login")
                .userParameter("nomUtilisateur").user(nom)
                .passwordParam("motDePasse").password(motDePasse);
    }

    private MockHttpServletRequestBuilder inscription(String nom, String motDePasse, String email) {
        return post("/register").with(csrf())
                .param("nomUtilisateur", nom)
                .param("motDePasse", motDePasse)
                .param("nomComplet", "Nom Complet")
                .param("adresseEmail", email);
    }

    @Test
    void lesComptesDeDepartSeConnectentAvecLeursRoles() throws Exception {
        mockMvc.perform(connexion("admin", "1234"))
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(redirectedUrl("/home"));
        mockMvc.perform(connexion("user", "1111"))
                .andExpect(authenticated().withUsername("user").withRoles("USER"))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void unMauvaisMotDePasseOuUnCompteInconnuEstRefuse() throws Exception {
        mockMvc.perform(connexion("user", "mauvais"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error=true"));
        mockMvc.perform(connexion("fantome", "1111"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void lesMotsDePasseSontHachesEnBase() {
        String hachage = userRepository.findByNomUtilisateur("admin").orElseThrow().getMotDePasseHache();

        assertTrue(hachage.startsWith("$2a$"));
    }

    @Test
    void leHachageNApparaitPasDansToString() {
        User compte = userRepository.findByNomUtilisateur("admin").orElseThrow();

        assertFalse(compte.toString().contains(compte.getMotDePasseHache()));
    }

    @Test
    void relancerLInitialisationNeCreeAucunDoublon() throws Exception {
        long comptes = userRepository.count();
        long roles = roleRepository.count();
        long profils = profilRepository.count();

        initialisation.run();

        assertEquals(comptes, userRepository.count());
        assertEquals(roles, roleRepository.count());
        assertEquals(profils, profilRepository.count());
    }

    @Test
    void sansConnexionLesPagesRedirigent() throws Exception {
        for (String page : new String[] {"/", "/home", "/user/dashboard", "/admin/dashboard"}) {
            mockMvc.perform(get(page)).andExpect(redirectedUrlPattern("**/login"));
        }
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void lesRolesLimitentLesEspaces() throws Exception {
        mockMvc.perform(get("/user/dashboard").with(user("user").roles("USER"))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/dashboard").with(user("user").roles("USER"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/dashboard").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void laDeconnexionRevientSurLogin() throws Exception {
        mockMvc.perform(logout("/logout")).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void uneInscriptionCreeUnCompteUserActifQuiPeutSeConnecter() throws Exception {
        mockMvc.perform(inscription("nouveau", "MotDePasse2026", "nouveau@example.com"))
                .andExpect(redirectedUrl("/login?registered=true"));

        User compte = userRepository.findByNomUtilisateur("nouveau").orElseThrow();
        assertTrue(compte.isCompteActif());
        assertEquals(1, compte.getRoles().size());
        assertEquals("ROLE_USER", compte.getRoles().iterator().next().getNomRole());
        assertTrue(profilRepository.findByUtilisateurNomUtilisateur("nouveau").isPresent());

        mockMvc.perform(connexion("nouveau", "MotDePasse2026")).andExpect(authenticated());
    }

    @Test
    void lInscriptionIgnoreUnRoleAjouteAuFormulaire() throws Exception {
        mockMvc.perform(inscription("pirate", "MotDePasse2026", "pirate@example.com").param("roles", "ROLE_ADMIN"))
                .andExpect(redirectedUrl("/login?registered=true"));

        mockMvc.perform(connexion("pirate", "MotDePasse2026"))
                .andExpect(authenticated().withRoles("USER"));
    }

    @Test
    void lesDoublonsEtLesDonneesInvalidesSontRefuses() throws Exception {
        long avant = userRepository.count();

        mockMvc.perform(inscription("admin", "MotDePasse2026", "autre@example.com"))
                .andExpect(content().string(containsString("déjà pris")));
        mockMvc.perform(inscription("autre", "MotDePasse2026", "admin@example.com"))
                .andExpect(content().string(containsString("déjà utilisée")));
        mockMvc.perform(inscription("a!", "court", "pas-un-mail"))
                .andExpect(content().string(containsString("3 à 30")))
                .andExpect(content().string(containsString("8 à 72")))
                .andExpect(content().string(containsString("n&#39;est pas valide")));
        mockMvc.perform(inscription("accents", "é".repeat(40), "accents@example.com"))
                .andExpect(content().string(containsString("trop long")));

        assertEquals(avant, userRepository.count());
    }

    @Test
    void unCompteDesactiveNePeutPlusSeConnecter() throws Exception {
        mockMvc.perform(inscription("bascule", "MotDePasse2026", "bascule@example.com"));
        Long id = userRepository.findByNomUtilisateur("bascule").orElseThrow().getIdUtilisateur();

        mockMvc.perform(post("/admin/comptes/" + id + "/activation").with(csrf())
                        .with(user("admin").authorities(() -> "COMPTES_GERER")))
                .andExpect(redirectedUrl("/admin/dashboard"));
        mockMvc.perform(connexion("bascule", "MotDePasse2026"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error=true"));

        mockMvc.perform(post("/admin/comptes/" + id + "/activation").with(csrf())
                .with(user("admin").authorities(() -> "COMPTES_GERER")));
        mockMvc.perform(connexion("bascule", "MotDePasse2026")).andExpect(authenticated());
    }

    @Test
    void laDesactivationExigeLaPermissionEtLeJetonCsrf() throws Exception {
        Long id = userRepository.findByNomUtilisateur("user").orElseThrow().getIdUtilisateur();

        mockMvc.perform(post("/admin/comptes/" + id + "/activation").with(csrf()).with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
        // Le rôle seul ne suffit pas : la permission COMPTES_GERER est exigée
        mockMvc.perform(post("/admin/comptes/" + id + "/activation").with(csrf()).with(user("x").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/comptes/" + id + "/activation")
                        .with(user("admin").authorities(() -> "COMPTES_GERER")))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.findByNomUtilisateur("user").orElseThrow().isCompteActif());
    }

    @Test
    void lAdministrateurDeDepartPossedeLaPermission() throws Exception {
        mockMvc.perform(connexion("admin", "1234"))
                .andExpect(authenticated().withAuthorities(java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("COMPTES_GERER"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    void unAdministrateurNePeutPasDesactiverSonPropreCompte() throws Exception {
        Long id = userRepository.findByNomUtilisateur("admin").orElseThrow().getIdUtilisateur();

        mockMvc.perform(post("/admin/comptes/" + id + "/activation").with(csrf())
                        .with(user("admin").authorities(() -> "COMPTES_GERER")))
                .andExpect(redirectedUrl("/admin/dashboard"));

        assertTrue(userRepository.findByNomUtilisateur("admin").orElseThrow().isCompteActif());
    }

    @Test
    void chacunModifieSeulementSonProfil() throws Exception {
        mockMvc.perform(post("/user/profil").with(csrf()).with(user("user").roles("USER"))
                        .param("nomComplet", "Nom Modifié").param("adresseEmail", "modifie@example.com"))
                .andExpect(redirectedUrl("/user/dashboard"));

        assertEquals("Nom Modifié", profilRepository.findByUtilisateurNomUtilisateur("user").orElseThrow().getNomComplet());
        assertEquals("Administrateur", profilRepository.findByUtilisateurNomUtilisateur("admin").orElseThrow().getNomComplet());

        // E-mail déjà pris par un autre compte : refus sans erreur 500
        mockMvc.perform(post("/user/profil").with(csrf()).with(user("user").roles("USER"))
                        .param("nomComplet", "Nom Modifié").param("adresseEmail", "admin@example.com"))
                .andExpect(redirectedUrl("/user/dashboard"));
    }
}
