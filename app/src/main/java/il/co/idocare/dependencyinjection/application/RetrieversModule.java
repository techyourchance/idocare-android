package il.co.idocare.dependencyinjection.application;

import android.content.ContentResolver;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import il.co.idocarecore.requests.retrievers.RawRequestRetriever;
import il.co.idocarecore.requests.retrievers.RequestsRetriever;
import il.co.idocarecore.requests.retrievers.TempIdRetriever;
import il.co.idocarecore.useractions.UserActionsToRequestsApplier;
import il.co.idocarecore.useractions.retrievers.UserActionsRetriever;
import il.co.idocarecore.users.UsersRetriever;

@Module
@InstallIn(ApplicationComponent.class)
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
