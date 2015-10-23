package il.co.idocare;

/**
 * This class encapsulates everything related to URLs that the app uses
 */
public class URLs {

    // Host
    private final static String DEV_ROOT_URL = "http://dev-04.idocare.co.il";
    private final static String QA_ROOT_URL = "http://qa-04.idocare.co.il";
    private final static String ROOT_URL = DEV_ROOT_URL;

    // Nodes
    private final static String NODE_SIGN_UP = "/api-04/user/add";
    private final static String NODE_LOG_IN = "/api-04/user/login";
    private final static String NODE_USER_DATA = "/api-04/user/get";

    private final static String NODE_ALL_REQUESTS_DATA = "/api-04/request";
    private final static String NODE_REQUEST_DATA = "/api-04/request/get";
    private final static String NODE_CREATE_REQUEST = "/api-04/request/add";
    private final static String NODE_PICKUP_REQUEST = "/api-04/request/pickup";
    private final static String NODE_CLOSE_REQUEST = "/api-04/request/close";
    private final static String NODE_VOTE_FOR_REQUEST = "/api-04/request/vote";

    private final static String NODE_ALL_ARTICLES_DATA = "/api-04/article";
    private final static String NODE_VOTE_FOR_ARTICLE = "/api-04/article/vote";

    // assigning indexes to resources used by the app
    private static final int USER_GROUP_OFFSET = 1000;
    public static final int RESOURCE_SIGNUP = USER_GROUP_OFFSET + 1;
    public static final int RESOURCE_LOGIN = USER_GROUP_OFFSET + 2;
    public static final int RESOURCE_USER_DATA = USER_GROUP_OFFSET + 3;

    private static final int REQUEST_GROUP_OFFSET = 2000;
    public static final int RESOURCE_ALL_REQUESTS_DATA = REQUEST_GROUP_OFFSET + 1;
    public static final int RESOURCE_REQUEST_DATA = REQUEST_GROUP_OFFSET + 2;
    public static final int RESOURCE_CREATE_REQUEST = REQUEST_GROUP_OFFSET + 3;
    public static final int RESOURCE_PICKUP_REQUEST = REQUEST_GROUP_OFFSET + 4;
    public static final int RESOURCE_CLOSE_REQUEST = REQUEST_GROUP_OFFSET + 5;
    public static final int RESOURCE_VOTE_FOR_REQUEST = REQUEST_GROUP_OFFSET + 6;


    private static final int ARTICLE_GROUP_OFFSET = 3000;
    public static final int RESOURCE_ALL_ARTICLES_DATA = ARTICLE_GROUP_OFFSET + 1;
    public static final int RESOURCE_VOTE_FOR_ARTICLE = ARTICLE_GROUP_OFFSET + 2;


    /**
     * @param resourceIndex the "index" of the resource that the url ir required for
     * @return the url corresponding to the provided resource's "index"
     * @throws IllegalArgumentException if the provided "index" does not correspond to any of the
     *         supported resources
     */
    public static String getUrl(int resourceIndex) {
        switch (resourceIndex) {
            case RESOURCE_SIGNUP:
                return constructUrl(ROOT_URL, NODE_SIGN_UP);
            case RESOURCE_LOGIN:
                return constructUrl(ROOT_URL, NODE_LOG_IN);
            case RESOURCE_USER_DATA:
                return constructUrl(ROOT_URL, NODE_USER_DATA);
            case RESOURCE_ALL_REQUESTS_DATA:
                return constructUrl(ROOT_URL, NODE_REQUEST_DATA);
            case RESOURCE_REQUEST_DATA:
                return constructUrl(ROOT_URL, NODE_REQUEST_DATA);
            case RESOURCE_ALL_ARTICLES_DATA:
                return constructUrl(ROOT_URL, NODE_ALL_ARTICLES_DATA);
            default:
                throw new IllegalArgumentException("getUrl was called with illegal resource index");
        }
    }

    /**
     * Adding this level of indirection just in case we would like to have some additional logic
     * in the future...
     */
    private static String constructUrl(String rootUrl, String node) {
        return rootUrl + node;
    }
}
