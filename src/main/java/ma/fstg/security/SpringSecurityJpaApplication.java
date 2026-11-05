package ma.fstg.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Démarre le serveur et charge les composants situés sous ce package. */
@SpringBootApplication
public class SpringSecurityJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityJpaApplication.class, args);
    }
}
