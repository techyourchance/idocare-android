package il.co.idocare;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import il.co.idocare.contentproviders.IdcSQLiteOpenHelper;
import il.co.idocare.dependencyinjection.application.ApplicationComponent;
import il.co.idocare.dependencyinjection.application.ApplicationModule;
import il.co.idocare.dependencyinjection.application.ContentProviderModule;
import il.co.idocare.dependencyinjection.application.DaggerApplicationComponent;
import il.co.idocare.dependencyinjection.application.CachersModule;
import il.co.idocare.dependencyinjection.application.NetworkingModule;
import il.co.idocare.dependencyinjection.application.RetrieversModule;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class IdcApplication extends Application {

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

        FacebookSdk.sdkInitialize(getApplicationContext());

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .networkingModule(new NetworkingModule())
                .contentProviderModule(new ContentProviderModule(IdcSQLiteOpenHelper.getInstance(this)))
                .cachersModule(new CachersModule())
                .retrieversModule(new RetrieversModule())
                .build();


        // TODO: alter the configuration of UIL according to our needs
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

}
