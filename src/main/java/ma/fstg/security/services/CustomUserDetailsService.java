package ma.fstg.security.services;

import lombok.RequiredArgsConstructor;
import ma.fstg.security.entities.Role;
import ma.fstg.security.entities.User;
import ma.fstg.security.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Indique à Spring Security comment retrouver un compte : ici, dans MySQL.
 * Spring compare ensuite lui-même le mot de passe saisi avec le hachage renvoyé.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String nomUtilisateur) throws UsernameNotFoundException {
        User compte = userRepository.findByNomUtilisateur(nomUtilisateur)
                .orElseThrow(() -> new UsernameNotFoundException("Compte inconnu : " + nomUtilisateur));

        // Rôles et permissions deviennent tous des autorisations pour Spring Security
        Set<SimpleGrantedAuthority> autorisations = new HashSet<>();
        for (Role role : compte.getRoles()) {
            autorisations.add(new SimpleGrantedAuthority(role.getNomRole()));
            role.getPermissions().forEach(permission ->
                    autorisations.add(new SimpleGrantedAuthority(permission.getCodePermission())));
        }

        // Le troisième argument bloque la connexion des comptes désactivés
        return new org.springframework.security.core.userdetails.User(
                compte.getNomUtilisateur(),
                compte.getMotDePasseHache(),
                compte.isCompteActif(),
                true, true, true,
                autorisations);
    }
}
