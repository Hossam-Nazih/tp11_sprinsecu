package ma.fstg.security.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Droit précis accordé à un rôle (ex. : COMPTES_GERER).
 * Une règle peut ainsi viser une action plutôt qu'un rôle entier.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPermission;

    @Column(nullable = false, unique = true, length = 50)
    private String codePermission;

    @Column(nullable = false)
    private String libelle;
}
