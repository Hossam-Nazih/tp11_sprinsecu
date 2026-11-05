package ma.fstg.security.services;

/** Inscription impossible ; le message est affiché tel quel à l'utilisateur. */
public class InscriptionRefuseeException extends RuntimeException {

    public InscriptionRefuseeException(String message) {
        super(message);
    }
}
