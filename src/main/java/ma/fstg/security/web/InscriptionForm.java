package ma.fstg.security.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Champs du formulaire d'inscription : aucun rôle ne peut y être transmis. */
public record InscriptionForm(
        @Pattern(regexp = "[A-Za-z0-9._-]{3,30}",
                message = "Le nom d'utilisateur contient 3 à 30 lettres, chiffres, points ou tirets")
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        String nomUtilisateur,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe contient 8 à 72 caractères")
        String motDePasse,

        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(max = 100, message = "Le nom complet ne dépasse pas 100 caractères")
        String nomComplet,

        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail n'est pas valide")
        @Size(max = 150, message = "L'adresse e-mail ne dépasse pas 150 caractères")
        String adresseEmail) {
}
