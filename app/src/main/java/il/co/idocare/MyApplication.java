package il.co.idocare;

import android.app.Application;

import il.co.idocare.dependencyinjection.components.ApplicationComponent;
import il.co.idocare.dependencyinjection.components.DaggerApplicationComponent;
import il.co.idocare.dependencyinjection.modules.ApplicationModule;

/**
 * Customized Application class
 */
public class MyApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

}
