package il.co.idocare.dependencyinjection.controllerscope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Custom scope for controller related components
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerScope {
}
