package il.co.idocare;

import android.app.Application;

import il.co.idocare.dependencyinjection.applicationscope.ApplicationComponent;
import il.co.idocare.dependencyinjection.applicationscope.ApplicationModule;
import il.co.idocare.dependencyinjection.applicationscope.DaggerApplicationComponent;
import il.co.idocare.dependencyinjection.datacache.CachersModule;
import il.co.idocare.dependencyinjection.datacache.RetrieversModule;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Customized Application class
 */
public class MyApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .cachersModule(new CachersModule())
                .retrieversModule(new RetrieversModule())
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

}
