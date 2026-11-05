package ma.fstg.security.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Informations personnelles d'un compte.
 * Séparées de User pour que les données de connexion restent seules dans leur table.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProfil;

    @Column(nullable = false, length = 100)
    private String nomComplet;

    @Column(nullable = false, unique = true, length = 150)
    private String adresseEmail;

    @Column(nullable = false)
    private LocalDate dateInscription;

    /** Clé étrangère unique : un compte possède un seul profil. */
    @ToString.Exclude
    @OneToOne(optional = false)
    @JoinColumn(name = "id_utilisateur", nullable = false, unique = true)
    private User utilisateur;
}
