package ma.fstg.security.repositories;

import ma.fstg.security.entities.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilRepository extends JpaRepository<Profil, Long> {

    // Suit la relation utilisateur -> nomUtilisateur
    Optional<Profil> findByUtilisateurNomUtilisateur(String nomUtilisateur);

    boolean existsByAdresseEmail(String adresseEmail);
}
