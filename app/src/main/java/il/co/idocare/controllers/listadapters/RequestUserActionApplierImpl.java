package il.co.idocare.controllers.listadapters;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.interfaces.RequestUserActionApplier;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserActionItem;

/**
 * Created by Vasiliy on 7/5/2015.
 */
public class RequestUserActionApplierImpl implements RequestUserActionApplier {




    @Override
    public RequestItem applyUserAction(RequestItem request, UserActionItem userAction) {
        switch (userAction.mActionType) {
            case IDoCareContract.UserActions.ACTION_TYPE_VOTE:
                int voteValue = Integer.valueOf(userAction.mActionParam);
                if (userAction.mEntityParam
                        .equals(IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CREATED))
                    request.setCreatedReputation(request.getCreatedReputation() + voteValue);
                else if (userAction.mEntityParam
                        .equals(IDoCareContract.UserActions.ENTITY_PARAM_REQUEST_CLOSED))
                    request.setClosedReputation(request.getClosedReputation() + voteValue);
                else
                    throw new IllegalArgumentException("unrecognized ENTITY_PARAM '" +
                            userAction.mEntityParam + "' for ACTION_TYPE '" + userAction.mActionType + "'");
                break;
            default:
                throw new IllegalArgumentException("unrecognized ENTITY_PARAM '" +
                        userAction.mEntityParam + "' for ACTION_TYPE '" + userAction.mActionType + "'");
        }

        return request;
    }
}
