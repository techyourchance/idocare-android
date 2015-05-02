package il.co.idocare.controllers.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import il.co.idocare.authenticators.AccountAuthenticator;

/**
 * Authentication service.
 * The whole purpose of this service is to instantiate AccountAuthenticator and provide a
 * context to it.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
