package il.co.idocare.controllers.listadapters;

import android.util.Log;

import il.co.idocare.contentproviders.IDoCareContract;
import il.co.idocare.controllers.interfaces.RequestUserActionApplier;
import il.co.idocare.pojos.RequestItem;
import il.co.idocare.pojos.UserActionItem;
import il.co.idocare.utils.UtilMethods;

/**
 * Created by Vasiliy on 7/5/2015.
 */
public class RequestUserActionApplierImpl implements RequestUserActionApplier {

    private static final String LOG_TAG = RequestUserActionApplierImpl.class.getSimpleName();



    @Override
    public RequestItem applyUserAction(RequestItem request, UserActionItem userAction) {
        switch (userAction.mActionType) {

            case IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST:
                // Nothing to do for created request - this data is stored in DB
                break;

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

            case IDoCareContract.UserActions.ACTION_TYPE_PICKUP_REQUEST:
                if (request.getPickedUpBy() == 0) {
                    request.setPickedUpBy(Long.valueOf(userAction.mActionParam));
                    request.setPickedUpAt(UtilMethods.formatDate(userAction.mTimestamp));
                } else {
                    Log.e(LOG_TAG, "tried to apply PICKUP_REQUEST user action to request which" +
                            "has already been picked up");
                }
                break;

            default:
                throw new IllegalArgumentException("unrecognized ENTITY_PARAM '" +
                        userAction.mEntityParam + "' for ACTION_TYPE '" + userAction.mActionType + "'");
        }

        return request;
    }
}
