package il.co.idocare.networking.interfaces;

/**
 *
 */
public interface LegacyServerResponseHandlerFactory {

    /**
     * Get a new instance of LegacyServerResponseHandler. This method might be called from multiple
     * threads, therefore implementations MUST ensure thread safety.
     * @param url the URL that returned the response. This URL will be used in order to determine
     *            the functionality of the returned LegacyServerResponseHandler
     * @return new instance of LegacyServerResponseHandler which can be used to handle the response from
     *         a concrete URL
     */
    public LegacyServerResponseHandler newInstance(String url);

}
