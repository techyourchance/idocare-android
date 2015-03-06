package il.co.idocare.controllers.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import il.co.idocare.Constants;
import il.co.idocare.controllers.fragments.HomeFragment;
import il.co.idocare.controllers.fragments.LoginFragment;
import il.co.idocare.controllers.fragments.AbstractFragment;
import il.co.idocare.R;
import il.co.idocare.controllers.fragments.NewRequestFragment;
import il.co.idocare.controllers.fragments.SplashFragment;
import il.co.idocare.models.RequestsMVCModel;
import il.co.idocare.models.UsersMVCModel;


public class IDoCareActivity extends Activity implements
        AbstractFragment.IDoCareFragmentCallback,
        FragmentManager.OnBackStackChangedListener {

    private static final String LOG_TAG = "IDoCareActivity";


    public GoogleApiClient mGoogleApiClient;

    private RequestsMVCModel mRequestsModel;

    private UsersMVCModel mUsersModel;

    // ---------------------------------------------------------------------------------------------
    //
    // Activity lifecycle management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getActionBar() != null) getActionBar().hide();

        setContentView(R.layout.activity_main);

        // Decide which fragment to show if the app is not restored
        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_FILE, MODE_PRIVATE);

            if (prefs.contains(Constants.FieldName.USER_ID.getValue()) &&
                    prefs.contains(Constants.FieldName.USER_PUBLIC_KEY.getValue())) {
                // Show splash screen if user details exist
                getFragmentManager().beginTransaction()
                        .add(R.id.frame_contents_no_padding, new SplashFragment())
                        .commit();
            } else {
                // Bring up login fragment
                getFragmentManager().beginTransaction()
                        .add(R.id.frame_contents_no_padding, new LoginFragment())
                        .commit();
            }

            getFragmentManager().executePendingTransactions();
        }

        initializeModels();

        initUniversalImageLoader();

        buildGoogleApiClient();

        setupDrawer();

        // This callback will be used to show/hide up (back) button in actionbar
        getFragmentManager().addOnBackStackChangedListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) mGoogleApiClient.connect();

        // TODO: verify that this call resolves the missing UP button when the activity is restarted
        onBackStackChanged();

        // Start periodic updates of requests' cache
        //scheduleRequestsCacheUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        boolean actionsVisibility = !drawerLayout.isDrawerVisible(Gravity.START);

        for(int i=0;i<menu.size();i++){
            menu.getItem(i).setVisible(actionsVisibility);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    // End of activity lifecycle management
    //
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    //
    // Back stack management


    @Override
    public void onBackStackChanged() {
        if (getActionBar() != null) {
            // Enable Up button only  if there are entries in the back stack
            boolean hasBackstackEntries = getFragmentManager().getBackStackEntryCount() > 0;
            getActionBar().setDisplayHomeAsUpEnabled(hasBackstackEntries);
        }
    }

    @Override
    public boolean onNavigateUp() {
        getFragmentManager().popBackStack();
        return true;
    }

    // End of back stack management
    //
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    //
    // Fragments management

    // TODO: maybe we need to preserve the state of the replaced fragments?
    @Override
    public void replaceFragment(Class<? extends AbstractFragment> claz, boolean addToBackStack,
                                Bundle args) {

        final ViewGroup viewGroup = (ViewGroup)
                ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        int activeFrameLayoutId;
        if (viewGroup.findViewById(R.id.frame_contents_no_padding).getVisibility() == View.VISIBLE) {
            activeFrameLayoutId = R.id.frame_contents_no_padding;
        } else {
            activeFrameLayoutId = R.id.frame_contents;
        }

        Fragment currFragment = getFragmentManager().findFragmentById(activeFrameLayoutId);
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (currFragment != null) {
            if (claz.isInstance(currFragment)) {
                // The currently shown fragment is the same as the new one - nothing to do
                Log.v(LOG_TAG, "the fragment " + claz.getSimpleName() + " is already shown");
                return;
            }
        }

        // Create new fragment
        Fragment newFragment;

        try {
            newFragment = claz.newInstance();
            if (args != null) newFragment.setArguments(args);
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (addToBackStack) ft.addToBackStack(null);
        // Change to a new fragment
        ft.replace(activeFrameLayoutId, newFragment, claz.getClass().getSimpleName());
        ft.commit();

    }


    @Override
    public RequestsMVCModel getRequestsModel() {
        return mRequestsModel;
    }

    @Override
    public UsersMVCModel getUsersModel() {
        return mUsersModel;
    }

    // End of fragments management
    //
    // ---------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------------
    //
    // Navigation drawer management




    /**
     * Initiate the navigation drawer
     */
    private void setupDrawer() {

        setupDrawerListView();

        setupDrawerAndActionBarDependencies();

    }


    private void setupDrawerListView() {

        final String[] entries = getResources().getStringArray(R.array.drawer_entries);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.drawer_contents);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                entries));

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Highlight the selected item and close the drawer
                drawerList.setItemChecked(position, true);
                drawerLayout.closeDrawer(drawerList);

                String chosenEntry = entries[position];

                // Can't do switch/case on Strings :(
                if (chosenEntry.equals(getResources().getString(R.string.drawer_entry_home))) {
                    replaceFragment(HomeFragment.class, false, null);
                }
                else if (chosenEntry.equals(getResources().getString(R.string.drawer_entry_new_request))) {
                    replaceFragment(NewRequestFragment.class, false, null);
                }
                else if (chosenEntry.equals(getResources().getString(R.string.drawer_entry_logout))) {
                    IDoCareActivity.this.logOutCurrentUser();
                }
                else {
                    Log.e(LOG_TAG, "drawer entry \"" + chosenEntry + "\" has no functionality");
                }

                // Clear back-stack
                // TODO: this is correct only if all entries in the drawer are "top level"
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
        });
    }

    private void setupDrawerAndActionBarDependencies() {

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View view, float v) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int state) {
            }
        });
    }

    // End of navigation drawer management
    //
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    //
    // User session management

    private void logOutCurrentUser() {
        SharedPreferences prefs =
                getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        prefs.edit().remove(Constants.FieldName.USER_ID.getValue()).apply();
        prefs.edit().remove(Constants.FieldName.USER_PUBLIC_KEY.getValue()).commit();
        replaceFragment(LoginFragment.class, false, null);

    }

    // End of user session management
    //
    // ---------------------------------------------------------------------------------------------



    /**
     * Handle the initiation of UIL (third party package under Apache 2.0 license)
     */
    private void initUniversalImageLoader() {
        // TODO: alter the configuration of UIL according to our needs
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(Constants.DEFAULT_DISPLAY_IMAGE_OPTIONS)
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * Initialize the client which will be used to connect to Google Play Services
     */
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initializeModels() {
        mRequestsModel = new RequestsMVCModel(this);
        mRequestsModel.initialize();
        mUsersModel = new UsersMVCModel(this);
    }

}
