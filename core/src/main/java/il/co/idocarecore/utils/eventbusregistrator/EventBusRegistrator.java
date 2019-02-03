package il.co.idocarecore.utils.eventbusregistrator;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import il.co.idocarecore.utils.Logger;

/**
 * This class handles registration/unregistration of objects annotated with
 * {@link EventBusRegistrable} to/from event bus
 */
public class EventBusRegistrator {

    private static final String TAG = "EventBusRegistrator";

    private static final boolean ENABLE_PERFORMANCE_LOGGING = false;

    @NonNull private final EventBus mEventBus;
    private Logger mLogger;

    public EventBusRegistrator(@NonNull EventBus eventBus, @NonNull Logger logger) {
        mEventBus = eventBus;
        mLogger = logger;
    }

    /**
     * Register all members (private, protected, public) of types annotated with
     * {@link EventBusRegistrable} annotation. Appropriate members of superclasses also included.
     * @param target the object who's members should be registered to EventBus
     */
    public void registerMembersOfAnnotatedType(@NonNull Object target) {

        long methodStartTime = System.currentTimeMillis();
        long registrationStartTime = 0;
        long totalRegistrationTime = 0;

        Class<?> targetType = target.getClass();

        List<Field> fieldsOfAnnotatedType = getAllFieldsOfAnnotatedType(targetType);

        for (Field field : fieldsOfAnnotatedType) {
            field.setAccessible(true);
            try {
                Object annotatedObject = field.get(target);
                if (!mEventBus.isRegistered(annotatedObject)) {
                    mLogger.d(TAG, "registering field on EventBus; field: " + field + "; parent: " + target);
                    registrationStartTime = System.currentTimeMillis();
                    mEventBus.register(annotatedObject);
                    totalRegistrationTime += System.currentTimeMillis() - registrationStartTime;
                }
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (targetType.isAnnotationPresent(EventBusRegistrable.class)){
            if (!mEventBus.isRegistered(target)){
                mLogger.d(TAG, "registering on EventBus; class: " + target );
                registrationStartTime = System.currentTimeMillis();
                mEventBus.register(target);
                totalRegistrationTime += System.currentTimeMillis() - registrationStartTime;
            }
        }

        if (ENABLE_PERFORMANCE_LOGGING) {
            long totalMethodExecTime = System.currentTimeMillis() - methodStartTime;
            long totalOverheadTime = totalMethodExecTime - totalRegistrationTime;

            mLogger.v(TAG, "registerMembersOfAnnotatedType on object  " + target.toString()
                    + "\nmethod execution time: " + totalMethodExecTime + " ms"
                    + "\ntotal overhead time: " + totalOverheadTime + " ms");
        }
    }

    /**
     * Unregister all members (private, protected, public) of types annotated with
     * {@link EventBusRegistrable} annotation. Appropriate members of superclasses also included.
     * @param target the object who's members should be unregistered from EventBus
     */
    public void unregisterMembersOfAnnotatedType(@NonNull Object target) {

        long methodStartTime = System.currentTimeMillis();
        long unregistrationStartTime = 0;
        long totalUnregistrationTime = 0;

        Class<?> targetType = target.getClass();

        List<Field> fieldsOfAnnotatedType = getAllFieldsOfAnnotatedType(targetType);

        for (Field field : fieldsOfAnnotatedType) {
            field.setAccessible(true);
            try {
                Object annotatedObject = field.get(target);
                if (mEventBus.isRegistered(annotatedObject)) {
                    mLogger.d(TAG, "unregistering field from EventBus; field: " + field + "; parent: " + target);
                    unregistrationStartTime = System.currentTimeMillis();
                    mEventBus.unregister(annotatedObject);
                    totalUnregistrationTime += System.currentTimeMillis() - unregistrationStartTime;
                }
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (targetType.isAnnotationPresent(EventBusRegistrable.class)){
            if (mEventBus.isRegistered(target)){
                mLogger.d(TAG, "unregistering from EventBus; class: " + target );
                unregistrationStartTime = System.currentTimeMillis();
                mEventBus.unregister(target);
                totalUnregistrationTime += System.currentTimeMillis() - unregistrationStartTime;
            }
        }

        if (ENABLE_PERFORMANCE_LOGGING) {
            long totalMethodExecTime = System.currentTimeMillis() - methodStartTime;
            long totalOverheadTime = totalMethodExecTime - totalUnregistrationTime;

            mLogger.v(TAG, "unregisterMembersOfAnnotatedType on object  " + target.toString()
                    + "\nmethod execution time: " + totalMethodExecTime + " ms"
                    + "\ntotal overhead time: " + totalOverheadTime + " ms");
        }

    }

    private List<Field> getAllFieldsOfAnnotatedType(Class<?> type) {
        List<Field> allFields = getAllFields(type);

        List<Field> fieldsOfAnnotatedType = new ArrayList<>();
        for (Field field : allFields) {
            if (field.getType().isAnnotationPresent(EventBusRegistrable.class)) {
                fieldsOfAnnotatedType.add(field);
            }
        }
        return fieldsOfAnnotatedType;
    }

    /**
     * @return all fields (private, public, etc.) declared for a class (incl. superclasses)
     */
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

}
