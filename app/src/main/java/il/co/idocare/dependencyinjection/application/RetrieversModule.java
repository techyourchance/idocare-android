package il.co.idocare.dependencyinjection.application;

import android.content.ContentResolver;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.requests.retrievers.RawRequestRetriever;
import il.co.idocare.requests.retrievers.RequestsRetriever;
import il.co.idocare.requests.retrievers.TempIdRetriever;
import il.co.idocare.useractions.UserActionsToRequestsApplier;
import il.co.idocare.useractions.retrievers.UserActionsRetriever;
import il.co.idocare.users.UsersRetriever;

@Module
public class RetrieversModule {

    @Provides
    public UserActionsToRequestsApplier userActionsToRequestsApplier() {
        return new UserActionsToRequestsApplier();
    }

    @Provides
    public RawRequestRetriever rawRequestRetriever(ContentResolver contentResolver) {
        return new RawRequestRetriever(contentResolver);
    }

    @Provides
    public UserActionsRetriever userActionsRetriever(ContentResolver contentResolver) {
        return new UserActionsRetriever(contentResolver);
    }

    @Provides
    public RequestsRetriever requestsRetriever(RawRequestRetriever rawRequestRetriever,
                                               UserActionsRetriever userActionsRetriever,
                                               UserActionsToRequestsApplier userActionsToRequestsApplier) {
        return new RequestsRetriever(rawRequestRetriever, userActionsRetriever, userActionsToRequestsApplier);
    }

    @Provides
    UsersRetriever usersRetriever(ContentResolver contentResolver) {
        return new UsersRetriever(contentResolver);
    }

    @Provides
    TempIdRetriever tempIdRetriever(ContentResolver contentResolver) {
        return new TempIdRetriever(contentResolver);
    }

}
