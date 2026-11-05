package ma.fstg.security.repositories;

import ma.fstg.security.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Optional oblige l'appelant à traiter le cas d'un compte inexistant
    Optional<User> findByNomUtilisateur(String nomUtilisateur);

    boolean existsByNomUtilisateur(String nomUtilisateur);

    List<User> findAllByOrderByNomUtilisateurAsc();
}
