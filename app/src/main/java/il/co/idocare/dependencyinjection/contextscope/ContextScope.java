package il.co.idocare.dependencyinjection.contextscope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Custom scope for context related components
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextScope {
}
