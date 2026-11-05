package ma.fstg.security.repositories;

import ma.fstg.security.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Renvoie null si le rôle n'existe pas encore
    Role findByNomRole(String nomRole);
}
