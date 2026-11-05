package ma.fstg.security.services;

import lombok.RequiredArgsConstructor;
import ma.fstg.security.entities.Profil;
import ma.fstg.security.entities.Role;
import ma.fstg.security.entities.User;
import ma.fstg.security.repositories.ProfilRepository;
import ma.fstg.security.repositories.RoleRepository;
import ma.fstg.security.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Inscription, profil et activation des comptes. */
@Service
@RequiredArgsConstructor
public class CompteService {

    private static final int LIMITE_OCTETS_BCRYPT = 72;
    private static final String ROLE_PAR_DEFAUT = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfilRepository profilRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crée le compte et son profil ensemble : la transaction évite un compte sans profil.
     * Le rôle est fixé ici, donc le formulaire ne permet pas de devenir administrateur.
     */
    @Transactional
    public User inscrire(String nomUtilisateur, String motDePasse, String nomComplet, String adresseEmail) {
        if (userRepository.existsByNomUtilisateur(nomUtilisateur)) {
            throw new InscriptionRefuseeException("Ce nom d'utilisateur est déjà pris");
        }
        if (profilRepository.existsByAdresseEmail(adresseEmail)) {
            throw new InscriptionRefuseeException("Cette adresse e-mail est déjà utilisée");
        }
        // BCrypt ne traite que 72 octets : une lettre accentuée en occupe plusieurs
        if (motDePasse.getBytes(StandardCharsets.UTF_8).length > LIMITE_OCTETS_BCRYPT) {
            throw new InscriptionRefuseeException("Le mot de passe est trop long");
        }

        Role roleParDefaut = roleRepository.findByNomRole(ROLE_PAR_DEFAUT);
        User compte = new User(null, nomUtilisateur, passwordEncoder.encode(motDePasse), true,
                new ArrayList<>(List.of(roleParDefaut)));
        compte = userRepository.save(compte);

        profilRepository.save(new Profil(null, nomComplet, adresseEmail, LocalDate.now(), compte));
        return compte;
    }

    public Optional<Profil> trouverProfil(String nomUtilisateur) {
        return profilRepository.findByUtilisateurNomUtilisateur(nomUtilisateur);
    }

    public Profil modifierProfil(String nomUtilisateur, String nomComplet, String adresseEmail) {
        Profil profil = profilRepository.findByUtilisateurNomUtilisateur(nomUtilisateur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil introuvable"));
        profil.setNomComplet(nomComplet);
        profil.setAdresseEmail(adresseEmail);
        return profilRepository.save(profil);
    }

    public List<User> listerComptes() {
        return userRepository.findAllByOrderByNomUtilisateurAsc();
    }

    /**
     * Active ou désactive un compte.
     * Un administrateur ne peut pas désactiver le sien : il perdrait l'accès à cette page.
     *
     * @return false si l'action visait le compte connecté
     */
    public boolean basculerActivation(Long idUtilisateur, String nomConnecte) {
        User compte = userRepository.findById(idUtilisateur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable"));
        if (compte.getNomUtilisateur().equals(nomConnecte)) {
            return false;
        }
        compte.setCompteActif(!compte.isCompteActif());
        userRepository.save(compte);
        return true;
    }
}
