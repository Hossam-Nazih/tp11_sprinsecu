package ma.fstg.security.config;

import ma.fstg.security.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Relie Spring Security à la base de données et fixe les règles d'accès.
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Cherche le compte avec notre service, puis vérifie le mot de passe avec BCrypt. */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider fournisseur = new DaoAuthenticationProvider(userDetailsService);
        fournisseur.setPasswordEncoder(passwordEncoder());
        return fournisseur;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(regles -> regles
                .requestMatchers("/login", "/register", "/css/**", "/error").permitAll()
                // La règle la plus précise doit précéder /admin/** pour être prise en compte
                .requestMatchers("/admin/comptes/**").hasAuthority("COMPTES_GERER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(connexion -> connexion
                .loginPage("/login")
                .usernameParameter("nomUtilisateur")
                .passwordParameter("motDePasse")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(deconnexion -> deconnexion
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
        return http.build();
    }
}
