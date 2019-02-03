package il.co.idocarecore.contentproviders;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import il.co.idocarecore.Constants;
import il.co.idocarecore.Constants;

/**
 * Contract class for our ContentProvider
 */
public class IDoCareContract {

	/**
	 * The authority of the requests provider.
	 */
	public static final String AUTHORITY = "il.co.idocarecore.provider.IDoCareContentProvider";

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
        public static final String COL_REQUEST_ID = Constants.FIELD_NAME_REQUEST_ID;
        public static final String COL_CREATED_BY = Constants.FIELD_NAME_CREATED_BY;
        public static final String COL_PICKED_UP_BY = Constants.FIELD_NAME_PICKED_UP_BY;
        public static final String COL_CREATED_AT = Constants.FIELD_NAME_CREATED_AT;
        public static final String COL_PICKED_UP_AT = Constants.FIELD_NAME_PICKED_UP_AT;
        public static final String COL_CLOSED_AT = Constants.FIELD_NAME_CLOSED_AT;
        public static final String COL_CREATED_COMMENT = Constants.FIELD_NAME_CREATED_COMMENT;
        public static final String COL_CLOSED_COMMENT = Constants.FIELD_NAME_CLOSED_COMMENT;
        public static final String COL_LATITUDE = Constants.FIELD_NAME_LATITUDE;
        public static final String COL_LONGITUDE = Constants.FIELD_NAME_LONGITUDE;
        public static final String COL_CREATED_PICTURES = Constants.FIELD_NAME_CREATED_PICTURES;
        public static final String COL_CLOSED_PICTURES = Constants.FIELD_NAME_CLOSED_PICTURES;
        public static final String COL_POLLUTION_LEVEL = Constants.FIELD_NAME_CREATED_POLLUTION_LEVEL;
        public static final String COL_CREATED_VOTES = Constants.FIELD_NAME_CREATED_REPUTATION;
        public static final String COL_CLOSED_VOTES = Constants.FIELD_NAME_CLOSED_REPUTATION;
        public static final String COL_CLOSED_BY = Constants.FIELD_NAME_CLOSED_BY;

        /**
         * This column contains a description of the location specified by LATITUDE and
         * LONGITUDE (reverse geocoding)
         */
        public static final String COL_LOCATION = Constants.FIELD_NAME_LOCATION;

		/**
		 * The content URI for this table.
		 */
		public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "requests");

		/**
		 * The mime type of a directory of requests.
		 */
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocarecore.requests";

		/**
		 * The mime type of a single request.
		 */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.il.co.idocarecore.requests";

		/**
         * A projection of all columns in the Requests table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
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
                COL_CREATED_VOTES,
                COL_CLOSED_VOTES,
                COL_CLOSED_BY,
                COL_LOCATION};

		/**
		 * The default sort order for queries containing NAME fields.
		 */
		public static final String SORT_ORDER_DEFAULT = COL_CREATED_AT + " ASC";
	}



    /**
     * Constants for the UserAction table of the provider.
     */
    public static final class Users implements BaseColumns {

        // Entry fields (correspond to columns in table)
        public static final String COL_USER_ID = Constants.FIELD_NAME_USER_ID;
        public static final String COL_USER_NICKNAME = Constants.FIELD_NAME_USER_NICKNAME;
        public static final String COL_USER_FIRST_NAME = Constants.FIELD_NAME_USER_FIRST_NAME;
        public static final String COL_USER_LAST_NAME = Constants.FIELD_NAME_USER_LAST_NAME;
        public static final String COL_USER_REPUTATION = Constants.FIELD_NAME_USER_REPUTATION;
        public static final String COL_USER_PICTURE = Constants.FIELD_NAME_USER_PICTURE;

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "users");

        /**
         * The mime type of a directory of user actions.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocarecore.users";

        /**
         * The mime type of a single user action.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.il.co.idocarecore.users";

        /**
         * A projection of all columns in the UserActions table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                COL_USER_ID,
                COL_USER_NICKNAME,
                COL_USER_FIRST_NAME,
                COL_USER_LAST_NAME,
                COL_USER_REPUTATION,
                COL_USER_PICTURE
        };

        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = _ID + " DESC";
    }


    /**
     * This table aggregates all unique user IDs referenced in the DB
     */
    public static final class UniqueUserIds implements BaseColumns {

        // Entry fields (correspond to columns in table)
        public static final String COL_USER_ID = Constants.FIELD_NAME_USER_ID;

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "unique_user_ids");

        /**
         * The mime type of a directory of user actions.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocarecore.unique_user_ids";

        /**
         * A projection of all columns in the UserActions table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                COL_USER_ID,
        };

        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = _ID + " DESC";
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
        public static final String COL_SERVER_RESPONSE_STATUS_CODE = "server_response_status_code";
        public static final String COL_SERVER_RESPONSE_REASON_PHRASE = "server_response_reason_phrase";
        public static final String COL_SERVER_RESPONSE_ENTITY = "server_response_entity";

        // Field values (correspond to particular values in table cells)
        // Any change/addition to these values should be reflected to UserActionsComparator
        public static final String ENTITY_TYPE_REQUEST = "entity_type_request";
        public static final String ENTITY_TYPE_ARTICLE = "entity_type_article";
        public static final String ENTITY_PARAM_REQUEST_CREATED = "created";
        public static final String ENTITY_PARAM_REQUEST_CLOSED = "closed";
        public static final String ACTION_TYPE_CREATE_REQUEST= "action_type_create_request";
        public static final String ACTION_TYPE_PICKUP_REQUEST = "action_type_pickup_request";
        public static final String ACTION_TYPE_CLOSE_REQUEST = "action_type_close_request";
        public static final String ACTION_TYPE_VOTE_FOR_REQUEST = "action_type_vote";

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "user_actions");

        /**
         * The mime type of a directory of user actions.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocarecore.user_actions";

        /**
         * The mime type of a single user action.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.il.co.idocarecore.user_action";

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


    /**
     * Constants for the UserAction table of the provider.
     */
    public static final class TempIdMappings implements BaseColumns {

        // Entry fields (correspond to columns in table)
        public static final String COL_TEMP_ID = "temp_id";
        public static final String COL_PERMANENT_ID = "permanent_id";

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(IDoCareContract.CONTENT_URI, "temp_id_mappings");

        /**
         * The mime type of a directory of user actions.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.il.co.idocarecore.temp_id_mappings";

        /**
         * The mime type of a single user action.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.il.co.idocarecore.temp_id_mappings";

        /**
         * A projection of all columns in the UserActions table.
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                COL_TEMP_ID,
                COL_PERMANENT_ID
        };

        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = _ID + " DESC";
    }


}
