package ma.fstg.security.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Champs du profil que le titulaire du compte peut modifier. */
public record ProfilForm(
        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(max = 100, message = "Le nom complet ne dépasse pas 100 caractères")
        String nomComplet,

        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail n'est pas valide")
        @Size(max = 150, message = "L'adresse e-mail ne dépasse pas 150 caractères")
        String adresseEmail) {
}
