package ma.fstg.security.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstg.security.services.CompteService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Espaces utilisateur et administrateur, protégés par SecurityConfig. */
@Controller
@RequiredArgsConstructor
public class EspaceController {

    private final CompteService compteService;

    @GetMapping("/user/dashboard")
    public String espaceUtilisateur(Principal connecte, Model model) {
        model.addAttribute("nomConnecte", connecte.getName());
        model.addAttribute("profil", compteService.trouverProfil(connecte.getName()).orElse(null));
        return "user-dashboard";
    }

    /** Le profil modifié est toujours celui du compte connecté : l'URL ne contient aucun identifiant. */
    @PostMapping("/user/profil")
    public String modifierProfil(@Valid ProfilForm formulaire, BindingResult erreurs,
                                 Principal connecte, RedirectAttributes redirection) {
        if (erreurs.hasErrors()) {
            redirection.addFlashAttribute("erreur", erreurs.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/user/dashboard";
        }
        try {
            compteService.modifierProfil(connecte.getName(), formulaire.nomComplet(), formulaire.adresseEmail());
            redirection.addFlashAttribute("succes", "Profil mis à jour");
        } catch (DataIntegrityViolationException doublon) {
            redirection.addFlashAttribute("erreur", "Cette adresse e-mail est déjà utilisée");
        }
        return "redirect:/user/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String espaceAdministrateur(Principal connecte, Model model) {
        model.addAttribute("nomConnecte", connecte.getName());
        model.addAttribute("comptes", compteService.listerComptes());
        return "admin-dashboard";
    }

    @PostMapping("/admin/comptes/{idUtilisateur}/activation")
    public String basculerActivation(@PathVariable("idUtilisateur") Long idUtilisateur, Principal connecte,
                                     RedirectAttributes redirection) {
        if (compteService.basculerActivation(idUtilisateur, connecte.getName())) {
            redirection.addFlashAttribute("succes", "Statut du compte modifié");
        } else {
            redirection.addFlashAttribute("erreur", "Vous ne pouvez pas désactiver votre propre compte");
        }
        return "redirect:/admin/dashboard";
    }
}
