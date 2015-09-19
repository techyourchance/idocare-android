package il.co.idocare.controllers.listadapters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.idocare.authentication.AccountAuthenticator;
import il.co.idocare.authentication.UserStateManager;
import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.interfaces.RequestUserActionApplier;
import il.co.idocare.controllers.interfaces.RequestsCombinedCursorAdapter;
import il.co.idocare.controllers.interfaces.UserUserActionApplier;
import il.co.idocare.datamodels.functional.RequestItem;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.datamodels.functional.UserActionItem;
import il.co.idocare.views.RequestThumbnailViewMVC;

/**
 * Customized CursorAdapter that is used for displaying the list of requests on HomeFragment.
 */
public class HomeListAdapter extends CursorAdapter implements
        RequestsCombinedCursorAdapter {

    private final static String LOG_TAG = HomeListAdapter.class.getSimpleName();

    private Cursor mUsersCursor;
    private Cursor mUserActionsCursor;

    private RequestUserActionApplier mRequestUserActionApplier;

    // TODO: make use of this applier or remove it completely!
    private UserUserActionApplier mUserUserActionApplier;

    private UserStateManager mUserStateManager;

    private Map<Long, UserItem> mUsersCache;
    private Map<Long, List<UserActionItem>> mUserActionsCache;

    public HomeListAdapter(Context context, Cursor cursor, int flags,
                           RequestUserActionApplier requestUserActionApplier,
                           UserUserActionApplier userUserActionApplier) {
        super(context, cursor, flags);

        mRequestUserActionApplier = requestUserActionApplier;
        mUserUserActionApplier = userUserActionApplier;

        mUserStateManager = new UserStateManager(context);

        // TODO: the below initial values are totally arbitrary. Reconsider the values or the implementation of caches
        mUsersCache = new HashMap<>(5);
        mUserActionsCache = new HashMap<>(1);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return new RequestThumbnailViewMVC(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RequestItem request;

        try {
            request = RequestItem.create(cursor);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // TODO: consider more sophisticated error handling (maybe destroy the view?)
            return;
        }

        if (mUserActionsCache.containsKey(request.getId())) {
            for (UserActionItem userAction : mUserActionsCache.get(request.getId())) {
                request = mRequestUserActionApplier.applyUserAction(request, userAction);
            }
        }

        // Set request's status
        request.setStatus(mUserStateManager.getActiveAccountUserId());

        ((RequestThumbnailViewMVC) view).bindRequestItem(request);

        UserItem createdByUser = getUser(request.getCreatedBy());
        if (createdByUser != null)
            ((RequestThumbnailViewMVC) view).bindCreatedByUser(createdByUser);
        else
            ((RequestThumbnailViewMVC) view).bindCreatedByUser(UserItem.createAnonymousUser());

        if (request.isPickedUp()) {
            UserItem pickedUpByUser = getUser(request.getPickedUpBy());
            if (pickedUpByUser != null)
                ((RequestThumbnailViewMVC) view).bindPickedUpByUser(pickedUpByUser);
            else
                ((RequestThumbnailViewMVC) view).bindPickedUpByUser(UserItem.createAnonymousUser());
        }
    }

    private UserItem getUser(long id) {
        // If user's info has been cached - use it
        if (mUsersCache.containsKey(id)) {
            return mUsersCache.get(id);
        }

        // Search users' Cursor for the data
        if (mUsersCursor != null && mUsersCursor.moveToFirst()) {
            do {
                if (mUsersCursor.getLong(
                        mUsersCursor.getColumnIndex(IDoCareContract.Users.COL_USER_ID)) == id) {
                    UserItem user = UserItem.create(mUsersCursor);
                    // Cache the data for future use
                    mUsersCache.put(user.getId(), user);
                    return user;
                }
            } while (mUsersCursor.moveToNext());
        }

        return null;
    }


    @Override
    public Cursor swapRequestsCursor(Cursor requestsCursor) {
        return this.swapCursor(requestsCursor);
    }

    @Override
    public Cursor swapUsersCursor(Cursor usersCursor) {
        // Clear users cache
        mUsersCache.clear();

        Cursor oldUsersCursor = mUsersCursor;
        mUsersCursor = usersCursor;
        notifyDataSetChanged();
        return oldUsersCursor;
    }

    @Override
    public Cursor swapUserActionsCursor(Cursor userActionsCursor) {
        mUserActionsCache.clear();

        if (userActionsCursor != null && userActionsCursor.moveToFirst()) {
            do {
                UserActionItem userAction = UserActionItem.create(userActionsCursor);
                if (userAction.mEntityType.equals(IDoCareContract.UserActions.ENTITY_TYPE_REQUEST)) {
                    // Create new list of actions for entity
                    if (!mUserActionsCache.containsKey(userAction.mEntityId))
                        mUserActionsCache.put(userAction.mEntityId, new ArrayList<UserActionItem>(1));

                    mUserActionsCache.get(userAction.mEntityId).add(userAction);
                }
            } while (userActionsCursor.moveToNext());
        }

        Cursor oldUserActionsCursor = mUserActionsCursor;
        mUserActionsCursor = userActionsCursor;
        notifyDataSetChanged();
        return oldUserActionsCursor;
    }

    @Override
    public RequestItem getRequestAtPosition(int position) {
        return RequestItem.create((Cursor) this.getItem(position));
    }

    // TODO: remove this in favor of an external dependency (Asana #47055190657814)
    public Account getActiveAccount(Context context) {
        Account[] accounts = AccountManager.get(context).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE_DEFAULT);
        if (accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }
}
