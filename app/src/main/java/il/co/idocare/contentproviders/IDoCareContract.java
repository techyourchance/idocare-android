package il.co.idocare.contentproviders;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import il.co.idocare.Constants.FieldName;

/**
 * Created by Vasiliy on 3/24/2015.
 */
public class IDoCareContract {

	/**
	 * The authority of the requests provider.
	 */
	public static final String AUTHORITY = "il.co.idocare.provider.IDoCareContentProvider";

	/**
	 * The content URI for the top-level authority.
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * A selection clause for ID based queries.
	 */
	public static final String SELECTION_ID_BASED = BaseColumns._ID + " = ? ";


	/**
	 * Constants for the Requests table of the provider.
	 */
	public static final class Requests implements BaseColumns {

        // Entry fields (correspond to columns in table)
        public static final String COL_REQUEST_ID = FieldName.REQUEST_ID.getValue();
        public static final String COL_CREATED_BY = FieldName.CREATED_BY.getValue();
        public static final String COL_PICKED_UP_BY = FieldName.PICKED_UP_BY.getValue();
        public static final String COL_CREATED_AT = FieldName.CREATED_AT.getValue();
        public static final String COL_PICKED_UP_AT = FieldName.PICKED_UP_AT.getValue();
        public static final String COL_CLOSED_AT = FieldName.CLOSED_AT.getValue();
        public static final String COL_CREATED_COMMENT = FieldName.CREATED_COMMENT.getValue();
        public static final String COL_CLOSED_COMMENT = FieldName.CLOSED_COMMENT.getValue();
        public static final String COL_LATITUDE = FieldName.LATITUDE.getValue();
        public static final String COL_LONGITUDE = FieldName.LONGITUDE.getValue();
        public static final String COL_CREATED_PICTURES = FieldName.CREATED_PICTURES.getValue();
        public static final String COL_CLOSED_PICTURES = FieldName.CLOSED_PICTURES.getValue();
        public static final String COL_POLLUTION_LEVEL = FieldName.CREATED_POLLUTION_LEVEL.getValue();
        public static final String COL_CREATED_REPUTATION = FieldName.CREATED_REPUTATION.getValue();
        public static final String COL_CLOSED_REPUTATION = FieldName.CLOSED_REPUTATION.getValue();
        public static final String COL_CLOSED_BY = FieldName.CLOSED_BY.getValue();


        /**
         * This column, when set to int>0, indicates that the corresponding request was modified
         * locally (potentially more than once), and that these local changes haven't been
         * synced to the server yet.
         */
        public static final String COL_MODIFIED_LOCALLY_FLAG = "modified_locally_flag";

		/**
		 * The content URI for this table.
		 */
		public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "requests");

		/**
		 * The mime type of a directory of requests.
		 */
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocare.requests";

		/**
		 * The mime type of a single request.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.il.co.idocare.requests";

		/**
         * A projection of all columns in the Requests table.
         */
        public static final String[] PROJECTION_ALL = {
                COL_REQUEST_ID,
                COL_CREATED_BY,
                COL_PICKED_UP_BY,
                COL_CREATED_AT,
                COL_PICKED_UP_AT,
                COL_CLOSED_AT,
                COL_CREATED_COMMENT,
                COL_CLOSED_COMMENT,
                COL_LATITUDE,
                COL_LONGITUDE,
                COL_CREATED_PICTURES,
                COL_CLOSED_PICTURES,
                COL_POLLUTION_LEVEL,
                COL_CREATED_REPUTATION,
                COL_CLOSED_REPUTATION,
                COL_CLOSED_BY};

        // TODO: add column containing "timestamp" (latest activity time) and make default sort by this column
		/**
		 * The default sort order for queries containing NAME fields.
		 */
		public static final String SORT_ORDER_DEFAULT = COL_CREATED_AT + " ASC";
	}


    /**
     * Constants for the UserAction table of the provider.
     */
    public static final class UserActions implements BaseColumns {

        // Entry fields (correspond to columns in table)
        public static final String COL_TIMESTAMP = "timestamp";
        public static final String COL_ENTITY_TYPE = "entity_type";
        public static final String COL_ENTITY_ID = "entity_id";
        public static final String COL_ENTITY_PARAM = "entity_param";
        public static final String COL_ACTION_TYPE = "action_type";
        public static final String COL_ACTION_PARAM = "action_param";

        // Field values (correspond to particular values in table cells)
        public static final String ENTITY_TYPE_REQUEST = "entity_type_request";
        public static final String ENTITY_TYPE_ARTICLE = "entity_type_article";
        public static final String ENTITY_PARAM_REQUEST_CREATED = "entity_param_request_created";
        public static final String ENTITY_PARAM_REQUEST_CLOSED = "entity_param_request_closed";
        public static final String ACTION_TYPE_CREATE_REQUEST= "action_type_create_request";
        public static final String ACTION_TYPE_PICKUP_REQUEST = "action_type_pickup_request";
        public static final String ACTION_TYPE_CLOSE_REQUEST = "action_type_close_request";
        public static final String ACTION_TYPE_VOTE = "action_type_vote";

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "user_actions");

        /**
         * The mime type of a directory of user actions.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocare.user_actions";

        /**
         * The mime type of a single user action.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.il.co.idocare.user_action";

        /**
         * A projection of all columns in the UserActions table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                COL_TIMESTAMP,
                COL_ENTITY_TYPE,
                COL_ENTITY_ID,
                COL_ENTITY_PARAM,
                COL_ACTION_TYPE,
                COL_ACTION_PARAM
        };

        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = COL_TIMESTAMP + " DESC";
    }


}
