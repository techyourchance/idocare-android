package il.co.idocare.controllers.interfaces;

import il.co.idocare.datamodels.functional.UserActionItem;
import il.co.idocare.datamodels.functional.UserItem;

/**
 * Created by Vasiliy on 7/5/2015.
 */
public interface UserUserActionApplier {

    public UserItem applyUserAction(UserItem user, UserActionItem userAction);
}
