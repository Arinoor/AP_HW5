package Exception;

public class InvalidPasswordException extends InvalidCredentialsException {
    public InvalidPasswordException (String message) {
        super(message);
    }
}
