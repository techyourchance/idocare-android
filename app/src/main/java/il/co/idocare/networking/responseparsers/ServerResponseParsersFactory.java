package il.co.idocare.networking.responseparsers;

import il.co.idocare.URLs;

/**
 * This factory instantiates the implementations of {@link ServerHttpResponseParser} interface
 */
public class ServerResponseParsersFactory {

    /**
     * @param resourceIndex one of the indices defined as RESOURCE_ in {@link URLs}
     * @return an implementation of {@link ServerHttpResponseParser} suitable for parsing the
     *         HTTP response from the specified resource
     * @throws IllegalArgumentException if the provided resource's index is illegal
     */
    public static ServerHttpResponseParser newInstance(int resourceIndex) {
        switch (resourceIndex) {
            case URLs.RESOURCE_SIGNUP:
                return new NativeSignupResponseParser();
            case URLs.RESOURCE_LOGIN:
                return new NativeLoginResponseParser();
            default:
                throw new IllegalArgumentException("newInstance was called with illegal resource index");
        }
    }
}
