package il.co.idocare.networking.interfaces;

/**
 *
 */
public interface ServerResponseHandlerFactory {

    /**
     * Get a new instance of ServerResponseHandler. This method might be called from multiple
     * threads, therefore implementations MUST ensure thread safety.
     * @param url the URL that returned the response. This URL will be used in order to determine
     *            the functionality of the returned ServerResponseHandler
     * @return new instance of ServerResponseHandler which can be used to handle the response from
     *         a concrete URL
     */
    public ServerResponseHandler newInstance(String url);

}
