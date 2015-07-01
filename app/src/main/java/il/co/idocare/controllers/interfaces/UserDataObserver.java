package il.co.idocare.controllers.interfaces;


import java.util.Set;

import il.co.idocare.pojos.UserItem;

/**
 * Instances implementing this interface can register with UserDataObservable objects and receive
 * notifications about changes in users' data
 */
public interface UserDataObserver {

    /**
     * This method is called when the notifyObservers() method of the specified UserDataObservable
     * is called.<br>
     * @param observable the object that called this method
     * @param updatedUsers the updated data of changed users (implementations MUST NOT assume
     *                     that this set is restricted to the set of IDs returned by
     *                     {@link UserDataObserver#getObservedUserIds()}
     */
    public void update(UserDataObservable observable, Set<UserItem> updatedUsers);

    /**
     * This method might be used by UserDataObservable in order to limit the change notifications
     * to the set of user IDs returned by this method.<br>
     * NOTE: if used, this method will be called during execution of
     * {@link UserDataObservable#addObserver(UserDataObserver)} and any subsequent changes to
     * the returned set will be ignored.
     * @return a set of user IDs which are of interest to this instance of UserDataObserver, or
     *         null if updates for all user IDs should be delivered.
     */
    public Set<Long> getObservedUserIds();

}
