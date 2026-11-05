package ma.fstg.security.config;

import ma.fstg.security.entities.Permission;
import ma.fstg.security.entities.Profil;
import ma.fstg.security.entities.Role;
import ma.fstg.security.entities.User;
import ma.fstg.security.repositories.PermissionRepository;
import ma.fstg.security.repositories.ProfilRepository;
import ma.fstg.security.repositories.RoleRepository;
import ma.fstg.security.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Crée les données de départ au démarrage.
 * Chaque élément n'est ajouté que s'il manque : les tables étant conservées d'un
 * lancement à l'autre, tout réinsérer créerait des doublons et bloquerait la connexion.
 */
@Configuration
public class DatabaseInitializer {

    @Bean
    CommandLineRunner init(PermissionRepository permissionRepo, RoleRepository roleRepo, UserRepository userRepo,
                           ProfilRepository profilRepo, BCryptPasswordEncoder encoder) {
        return args -> {
            Permission gererComptes = permissionRepo.findByCodePermission("COMPTES_GERER");
            if (gererComptes == null) {
                gererComptes = permissionRepo.save(
                        new Permission(null, "COMPTES_GERER", "Activer et désactiver les comptes"));
            }

            Role roleAdmin = creerRoleSiAbsent(roleRepo, "ROLE_ADMIN", List.of(gererComptes));
            Role roleUser = creerRoleSiAbsent(roleRepo, "ROLE_USER", List.of());

            creerCompteSiAbsent(userRepo, profilRepo, encoder, "admin", "1234", "Administrateur",
                    "admin@example.com", List.of(roleAdmin, roleUser));
            creerCompteSiAbsent(userRepo, profilRepo, encoder, "user", "1111", "Utilisateur de test",
                    "user@example.com", List.of(roleUser));
        };
    }

    private Role creerRoleSiAbsent(RoleRepository roleRepo, String nomRole, List<Permission> permissions) {
        Role role = roleRepo.findByNomRole(nomRole);
        return role != null ? role : roleRepo.save(new Role(null, nomRole, new ArrayList<>(permissions)));
    }

    private void creerCompteSiAbsent(UserRepository userRepo, ProfilRepository profilRepo,
                                     BCryptPasswordEncoder encoder, String nom, String motDePasse,
                                     String nomComplet, String adresseEmail, List<Role> roles) {
        if (userRepo.existsByNomUtilisateur(nom)) {
            return;
        }
        User compte = userRepo.save(new User(null, nom, encoder.encode(motDePasse), true, new ArrayList<>(roles)));
        profilRepo.save(new Profil(null, nomComplet, adresseEmail, LocalDate.now(), compte));
    }
}
