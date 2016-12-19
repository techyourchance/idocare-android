package il.co.idocare.useractions.entities;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import il.co.idocare.Constants;
import il.co.idocare.contentproviders.IDoCareContract.UserActions;
import il.co.idocare.utils.StringUtils;

/**
 * This class represents "close request" action executed by the user
 */
public class CloseRequestUserActionEntity extends UserActionEntity {

    private final String mClosedByUserId;
    private final String mClosedComment;
    private final List<String> mClosedPictures;

    public CloseRequestUserActionEntity(long timestamp,
                                        @NonNull String requestId,
                                        @NonNull String closedByUserId,
                                        @NonNull String closedComment,
                                        @NonNull List<String> closedPictures) {
        super(timestamp,
                UserActions.ENTITY_TYPE_REQUEST,
                requestId,
                null,
                UserActions.ACTION_TYPE_CLOSE_REQUEST,
                assembleActionParam(closedByUserId, closedComment, closedPictures));

        mClosedByUserId = closedByUserId;
        mClosedComment = closedComment;
        mClosedPictures = new ArrayList<>(closedPictures);
    }

    public String getClosedAt() {
        return String.valueOf(getTimestamp());
    }

    public String getClosedByUserId() {
        return mClosedByUserId;
    }

    public String getClosedComment() {
        return mClosedComment;
    }

    public List<String> getClosedPictures() {
        return Collections.unmodifiableList(mClosedPictures);
    }


    /**
     * Create JSON object containing user ID, comment and pictures
     */
    private static String assembleActionParam(@NonNull String closedByUserId,
                                              @NonNull String closedComment,
                                              @NonNull List<String> closedPictures) {
        return "{"
                + "\"" + Constants.FIELD_NAME_CLOSED_BY + "\" : \"" + closedByUserId + "\"" + ","
                + "\"" + Constants.FIELD_NAME_CLOSED_COMMENT + "\" : \"" + closedComment + "\"" + ","
                + "\"" + Constants.FIELD_NAME_CLOSED_PICTURES + "\" : \"" + StringUtils.listToCommaSeparatedString(closedPictures) + "\""
                + "}";
    }

}
