package il.co.idocare.dependencyinjection.controller;

import android.app.Activity;

import com.techyourchance.fragmenthelper.FragmentContainerWrapper;
import com.techyourchance.fragmenthelper.FragmentHelper;

import org.greenrobot.eventbus.EventBus;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import il.co.idocare.screens.ScreensNavigatorImpl;
import il.co.idocare.screens.common.fragments.MyFragmentFactory;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.InfoDialog;
import il.co.idocarecore.screens.common.dialogs.PromptDialog;

@Module
@InstallIn(ActivityComponent.class)
public abstract class ScreensNavigationModule {

    @Provides
    static FragmentContainerWrapper fragmentContainerWrapper(Activity activity) {
        return (FragmentContainerWrapper) activity;
    }

    @Provides
    static FragmentHelper fragmentHelper(Activity activity,
                                         FragmentContainerWrapper fragmentContainerWrapper,
                                         FragmentManager fragmentManager) {
        return new FragmentHelper(activity, fragmentContainerWrapper, fragmentManager);
    }

    @Binds
    abstract FragmentFactory fragmentFactory(MyFragmentFactory fragmentFactoryImpl);

    @Provides
    static ScreensNavigator screensNavigator(FragmentHelper fragmentHelper, Activity activity, FragmentFactory fragmentFactory) {
        return new ScreensNavigatorImpl(fragmentHelper, activity, fragmentFactory);
    }
    
    @Provides 
    static InfoDialog infoDialog(EventBus eventBus) {
        return new InfoDialog(eventBus);
    }
    
    @Provides
    static PromptDialog promptDialog(EventBus eventBus) {
        return new PromptDialog(eventBus);
    }
}
