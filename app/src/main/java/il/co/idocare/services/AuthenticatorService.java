package il.co.idocare.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import il.co.idocarecore.authentication.StubAccountAuthenticator;

/**
 * Authentication service.
 * The whole purpose of this service is to instantiate StubAccountAuthenticator and provide a
 * context to it.
 */
public class AuthenticatorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        StubAccountAuthenticator authenticator = new StubAccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
