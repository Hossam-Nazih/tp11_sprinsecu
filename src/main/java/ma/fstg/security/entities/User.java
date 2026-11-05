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
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Compte de connexion. Un compte peut cumuler plusieurs rôles.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    /** Unique : un doublon ferait échouer la recherche du compte lors de la connexion. */
    @Column(nullable = false, unique = true, length = 30)
    private String nomUtilisateur;

    /** Exclu de toString : sans cela, @Data écrirait le hachage dans les journaux. */
    @ToString.Exclude
    @Column(nullable = false)
    private String motDePasseHache;

    /** Passer ce champ à false bloque la connexion sans supprimer le compte. */
    private boolean compteActif;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();
}
