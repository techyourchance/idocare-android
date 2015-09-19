package il.co.idocare.controllers.listadapters;

import il.co.idocare.controllers.interfaces.UserUserActionApplier;
import il.co.idocare.datamodels.functional.UserItem;
import il.co.idocare.datamodels.functional.UserActionItem;

/**
 * Created by Vasiliy on 7/5/2015.
 */
public class UserActionsOnUserApplierImpl implements UserUserActionApplier {


    @Override
    public UserItem applyUserAction(UserItem user, UserActionItem userAction) {
        // TODO: complete this method
        return user;
    }
}
