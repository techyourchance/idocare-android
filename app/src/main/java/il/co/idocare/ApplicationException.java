package il.co.idocare;

/**
 * This is the base class for all custom exceptions thrown in the app.<br>
 * If any checked exceptions thrown by Android APIs should be propagated to top level modules,
 * the recommendation is to wrap that wrap that exception in this one (or any of its subclasses)<br>
 * Please note that this exception is not checked, which implies that any part of code
 * could throw this exception (or any of its subclasses). Be defensive!
 */
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
