package il.co.idocare.dependencyinjection.serversync;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Custom scope for "server sync" related components
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerSyncScope {
}
