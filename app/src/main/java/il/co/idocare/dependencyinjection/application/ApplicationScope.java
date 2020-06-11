package il.co.idocare.dependencyinjection.application;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;
import javax.inject.Singleton;

import dagger.hilt.migration.AliasOf;

/**
 * Custom scope for global application singletons
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@AliasOf(Singleton.class)
public @interface ApplicationScope {
}
