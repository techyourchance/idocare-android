package il.co.idocare.controllers.listadapters;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.idocare.Constants;
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

            case IDoCareContract.UserActions.ACTION_TYPE_CLOSE_REQUEST:
                if (request.getClosedBy() == 0) {
                    String closedBy;
                    String closedComment;
                    String closedPictures;
                    try {
                        JSONObject userActionParamJson = new JSONObject(userAction.mActionParam);
                        closedBy = userActionParamJson.getString(Constants.FIELD_NAME_CLOSED_BY);
                        closedComment = userActionParamJson.getString(Constants.FIELD_NAME_CLOSED_COMMENT);
                        closedPictures = userActionParamJson.getString(Constants.FIELD_NAME_CLOSED_PICTURES);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, "Couldn't parse CLOSE_REQUEST action's param as JSON");
                        break;
                    }

                    request.setClosedBy(Long.valueOf(closedBy));
                    request.setClosedAt(UtilMethods.formatDate(userAction.mTimestamp));
                    request.setClosedComment(closedComment);
                    request.setClosedPictures(closedPictures);
                } else {
                    Log.e(LOG_TAG, "tried to apply CLOSE_REQUEST user action to request which" +
                            "has already been closed");
                }
                break;

            default:
                throw new IllegalArgumentException("unrecognized ENTITY_PARAM '" +
                        userAction.mEntityParam + "' for ACTION_TYPE '" + userAction.mActionType + "'");
        }

        return request;
    }
}
