package ma.fstg.security.repositories;

import ma.fstg.security.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Permission findByCodePermission(String codePermission);
}
