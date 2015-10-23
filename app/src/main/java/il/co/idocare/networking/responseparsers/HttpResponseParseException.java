package il.co.idocare.networking.responseparsers;

import il.co.idocare.ApplicationException;

/**
 * A general exception thrown by implementations of {@link ServerHttpResponseParser}
 */
public class HttpResponseParseException extends ApplicationException {
    public HttpResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
