package il.co.idocare.useractions.entities;

import android.support.annotation.NonNull;

import il.co.idocare.contentproviders.IDoCareContract;

/**
 * This entity encapsulates information about a created request
 */
public class CreateRequestUserActionEntity extends UserActionEntity {

    public CreateRequestUserActionEntity(String timestamp,
                                         String requestId,
                                         String createdByUserId) {
        super(timestamp,
                IDoCareContract.UserActions.ENTITY_TYPE_REQUEST,
                requestId,
                null,
                IDoCareContract.UserActions.ACTION_TYPE_CREATE_REQUEST,
                createdByUserId);
    }

    public String getCreatedByUserId() {
        return getActionParam();
    }

}
