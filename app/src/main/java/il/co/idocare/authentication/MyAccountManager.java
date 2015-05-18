package il.co.idocare.authentication;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import il.co.idocare.Constants;

/**
 * Created by Vasiliy on 5/17/2015.
 */
public class MyAccountManager {

    

    public static AccountManagerFuture<Bundle> getAuthTokenForActiveAccount(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("the argument mustn't be null");

        GetAuthTokenForActiveAccountTask task = new GetAuthTokenForActiveAccountTask();

        return task;

    }


    private static class GetAuthTokenForActiveAccountTask extends FutureTask<Bundle> implements
            AccountManagerFuture<Bundle> {

        public GetAuthTokenForActiveAccountTask() {
            super(new Callable<Bundle>() {
                public Bundle call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
        }

        @Override
        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return null;
        }

        @Override
        public Bundle getResult(long l, TimeUnit timeUnit) throws OperationCanceledException, IOException, AuthenticatorException {
            return null;
        }
    }

//    {
//
//
//        SharedPreferences prefs =
//                activity.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
//
//        // Retrieve the entry for active account from the SharedPreferences
//        String accountName = prefs.getString(AccountManager.KEY_ACCOUNT_NAME, "");
//        String accountType = prefs.getString(AccountManager.KEY_ACCOUNT_TYPE, "");
//
//        Account account;
//
//        // Ensure that there is a valid entry for active account
//        if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType)) {
//            // TODO: pop up account picker screen if there are IDoCare accounts, log in screen if not
//            return addNewAccount();
//        }
//
//        account = new Account(accountName, accountType);
//        Account[] existingAccounts = AccountManager.get(this).getAccountsByType(accountType);
//
//        // Ensure that the active account is still registered on the device
//        if (!Arrays.asList(existingAccounts).contains(account)) {
//            // TODO: pop up account picker screen if there are IDoCare accounts, log in screen if not
//            // TODO: consider using the data of active account for creating the same account again
//            return addNewAccount();
//        }
//
//        return AccountManager.get(this).getAuthToken(
//                account,
//                AccountAuthenticator.ACCOUNT_TYPE,
//                null,
//                this,
//                null,
//                null);
//    }







}
