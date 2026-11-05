package ma.fstg.security.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstg.security.services.CompteService;
import ma.fstg.security.services.InscriptionRefuseeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

/** Connexion, inscription et page d'accueil. */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final CompteService compteService;

    @GetMapping("/login")
    public String pageConnexion() {
        return "login";
    }

    @GetMapping("/register")
    public String pageInscription() {
        return "register";
    }

    @PostMapping("/register")
    public String inscrire(@Valid @ModelAttribute("formulaire") InscriptionForm formulaire,
                           BindingResult erreurs, Model model) {
        if (erreurs.hasErrors()) {
            model.addAttribute("erreurs", erreurs.getAllErrors().stream()
                    .map(erreur -> erreur.getDefaultMessage()).distinct().toList());
            return "register";
        }
        try {
            compteService.inscrire(formulaire.nomUtilisateur(), formulaire.motDePasse(),
                    formulaire.nomComplet(), formulaire.adresseEmail());
        } catch (InscriptionRefuseeException refus) {
            model.addAttribute("erreurs", java.util.List.of(refus.getMessage()));
            return "register";
        } catch (DataIntegrityViolationException doublon) {
            // Deux inscriptions simultanées : la contrainte unique de la base tranche
            model.addAttribute("erreurs", java.util.List.of("Ce nom d'utilisateur ou cet e-mail est déjà utilisé"));
            return "register";
        }
        return "redirect:/login?registered=true";
    }

    /** Sans cette route, la racine du site répondrait 404 une fois connecté. */
    @GetMapping("/")
    public String racine() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String accueil(Principal connecte, Model model) {
        model.addAttribute("nomConnecte", connecte.getName());
        return "home";
    }
}
