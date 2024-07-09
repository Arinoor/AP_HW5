package Exception;

public class InvalidUsernameException extends InvalidCredentialsException {
    public InvalidUsernameException (String message) {
        super(message);
    }
}
