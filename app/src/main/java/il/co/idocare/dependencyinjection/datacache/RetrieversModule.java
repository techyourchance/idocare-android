package il.co.idocare.dependencyinjection.datacache;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.requests.retrievers.RequestsRetriever;

@Module
public class RetrieversModule {

    @Provides
    public RequestsRetriever requestsRetriever() {
        return new RequestsRetriever();
    }
}
