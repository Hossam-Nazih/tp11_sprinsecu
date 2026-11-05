package ma.fstg.security;

import ma.fstg.security.entities.Permission;
import ma.fstg.security.entities.Role;
import ma.fstg.security.entities.User;
import ma.fstg.security.repositories.UserRepository;
import ma.fstg.security.services.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Teste la conversion d'un compte en UserDetails, sans base de données. */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void lesRolesEtLesPermissionsDeviennentDesAutorisations() {
        Permission permission = new Permission(1L, "COMPTES_GERER", "Gérer");
        Role role = new Role(1L, "ROLE_ADMIN", List.of(permission));
        when(userRepository.findByNomUtilisateur("admin"))
                .thenReturn(Optional.of(new User(1L, "admin", "hachage", true, List.of(role))));

        UserDetails details = service.loadUserByUsername("admin");

        Set<String> autorisations = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_ADMIN", "COMPTES_GERER"), autorisations);
    }

    @Test
    void unCompteDesactiveEstSignaleASpringSecurity() {
        when(userRepository.findByNomUtilisateur("user"))
                .thenReturn(Optional.of(new User(2L, "user", "hachage", false, List.of())));

        assertFalse(service.loadUserByUsername("user").isEnabled());
    }

    @Test
    void unCompteInconnuLeveUneException() {
        when(userRepository.findByNomUtilisateur("fantome")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("fantome"));
    }
}
