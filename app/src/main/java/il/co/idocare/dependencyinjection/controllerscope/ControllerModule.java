package il.co.idocare.dependencyinjection.controllerscope;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;
import il.co.idocare.deviceinfo.GooglePlayServicesChecker;
import il.co.idocare.pictures.CameraAdapter;

@Module
public class ControllerModule {

    private Activity mActivity;

    public ControllerModule(@NonNull Activity activity) {
        mActivity = activity;
    }

    @Provides
    @ControllerScope
    Context provideContext() {
        return mActivity;
    }

    @Provides
    @ControllerScope
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    @ControllerScope
    GooglePlayServicesChecker provideGooglePlayServicesChecker(Activity activity) {
        return new GooglePlayServicesChecker(activity);
    }

    @Provides
    CameraAdapter cameraAdapter(Activity activity) {
        return new CameraAdapter(activity);
    }

}
