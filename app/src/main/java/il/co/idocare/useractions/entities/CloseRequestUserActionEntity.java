package il.co.idocare.useractions.entities;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import il.co.idocare.contentproviders.IDoCareContract.UserActions;
import il.co.idocare.utils.StringUtils;

/**
 * This class represents "close request" action executed by the user
 */
public class CloseRequestUserActionEntity extends UserActionEntity {

    private final String mClosedByUserId;
    private final String mClosedComment;
    private final List<String> mClosedPictures;

    public CloseRequestUserActionEntity(String timestamp,
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

    public static CloseRequestUserActionEntity fromUserAction(UserActionEntity userAction) {
        Gson gson = new Gson(); // TODO: this is inefficient

        ActionParam actionParam = gson.fromJson(userAction.getActionParam(), ActionParam.class);

        return new CloseRequestUserActionEntity(
                userAction.getDatetime(),
                userAction.getEntityId(),
                actionParam.closedBy,
                actionParam.closedComment,
                StringUtils.commaSeparatedStringToList(actionParam.closedPictures));

    }

    public String getClosedAt() {
        return String.valueOf(getDatetime());
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

    private static String assembleActionParam(String closedByUserId,
                                              String closedComment,
                                              List<String> closedPictures) {

        Gson gson = new Gson(); // TODO: this is inefficient

        ActionParam actionParam = new ActionParam(
                closedByUserId,
                closedComment,
                StringUtils.listToCommaSeparatedString(closedPictures)
        );

        return gson.toJson(actionParam);
    }

    private static class ActionParam {
        @SerializedName("closed_by") private String closedBy;
        @SerializedName("closed_comment") private String closedComment;
        @SerializedName("closed_pictures") private String closedPictures;

        public ActionParam(String closedBy, String closedComment, String closedPictures) {
            this.closedBy = closedBy;
            this.closedComment = closedComment;
            this.closedPictures = closedPictures;
        }
    }

}
