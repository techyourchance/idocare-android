package il.co.idocarecore.screens;

public interface ScreensNavigator {

    void toRequestDetails(String requestId);

    void toCloseRequest(String requestId, double longitude, double latitude);

    void toLogin();

    void toNewRequest();

    void toMyRequests();

    void toAllRequests();

    void navigateUp();

    void navigateBack();
}
