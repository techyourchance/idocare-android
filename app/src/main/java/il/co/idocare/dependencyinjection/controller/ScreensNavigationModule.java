package il.co.idocare.dependencyinjection.controller;

import android.app.Activity;

import org.greenrobot.eventbus.EventBus;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import il.co.idocare.screens.ScreensNavigatorImpl;
import il.co.idocare.screens.common.fragments.FragmentFactoryImpl;
import il.co.idocarecore.screens.ScreensNavigator;
import il.co.idocarecore.screens.common.dialogs.InfoDialog;
import il.co.idocarecore.screens.common.dialogs.PromptDialog;
import il.co.idocarecore.screens.common.fragmenthelper.FragmentContainerWrapper;
import il.co.idocarecore.screens.common.fragmenthelper.FragmentHelper;

@Module
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
    abstract FragmentFactory fragmentFactory(FragmentFactoryImpl fragmentFactoryImpl);

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
