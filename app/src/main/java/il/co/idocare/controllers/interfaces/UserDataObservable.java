package il.co.idocare.controllers.interfaces;

import java.util.Set;

import il.co.idocare.pojos.UserItem;

/**
 * Instances of this class can be used to notify instances of UserDataObserver about changes in
 * users' data.
 */
public interface UserDataObservable {

    /**
     * Adds the specified observer to the list of observers.<br>
     * NOTE: implementations might optionally account for the set of IDs returned by
     * {@link UserDataObserver#getObservedUserIds()} during new observer's addition
     *
     * @param observer the observer to add
     */
    public void addObserver(UserDataObserver observer);

    public void deleteObserver(UserDataObserver observer);

    public void deleteObservers();

    /**
     * Notify observers about change in users' data.
     * @param updatedUsers the updated data of changed users
     */
    public void notifyObservers(Set<UserItem> updatedUsers);
}
