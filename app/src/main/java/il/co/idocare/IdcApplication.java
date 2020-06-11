package il.co.idocare;

import com.facebook.FacebookSdk;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import androidx.multidex.MultiDexApplication;
import dagger.hilt.android.HiltAndroidApp;
import il.co.idocarecore.Constants;
import il.co.idocarecore.contentproviders.IdcSQLiteOpenHelper;
import il.co.idocare.dependencyinjection.application.ApplicationModule;
import il.co.idocare.dependencyinjection.application.ContentProviderModule;
import il.co.idocare.dependencyinjection.application.CachersModule;
import il.co.idocare.dependencyinjection.application.NetworkingModule;
import il.co.idocare.dependencyinjection.application.RetrieversModule;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

@HiltAndroidApp
public class IdcApplication extends MultiDexApplication {

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

        // TODO: alter the configuration of UIL according to our needs
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS)
                .build();
        ImageLoader.getInstance().init(config);
    }

}
