package il.co.idocarecore.utils.eventbusregistrator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used for annotating classes that have @Subscribe annotated methods,
 * but do not register themselves on event bus.<br>
 * The class that performs actual registration of objects annotated with this annotation
 * is {@link EventBusRegistrator}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventBusRegistrable {}
