package il.co.idocare.networking;

public final class HttpUtils {


    private HttpUtils() {}

    public static boolean isClientSideError(int httpResponseCode) {
        return httpResponseCode / 100 == 4;
    }
}
