package il.co.idocare.controllers.interfaces;

import il.co.idocare.pojos.UserActionItem;
import il.co.idocare.pojos.UserItem;

/**
 * Created by Vasiliy on 7/5/2015.
 */
public interface UserUserActionApplier {

    public UserItem applyUserAction(UserItem user, UserActionItem userAction);
}
