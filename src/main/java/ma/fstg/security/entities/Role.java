package ma.fstg.security.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Rôle attribué aux comptes.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRole;

    /** Le préfixe ROLE_ est obligatoire en base : hasRole("ADMIN") cherche ROLE_ADMIN. */
    @Column(nullable = false, unique = true, length = 50)
    private String nomRole;

    /** Chargées avec le rôle, car la connexion en a besoin après la fin de la transaction. */
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Permission> permissions = new ArrayList<>();
}
