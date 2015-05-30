package il.co.idocare.contentproviders;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.BaseColumns;

import il.co.idocare.Constants.FieldName;
import il.co.idocare.R;

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
	public static final String SELECTION_ID_BASED = Requests.REQUEST_ID + " = ? ";


	/**
	 * Constants for the Requests table of the provider.
	 */
	public static final class Requests implements BaseColumns {

        public static final String REQUEST_ID = FieldName.REQUEST_ID.getValue();
        public static final String CREATED_BY = FieldName.CREATED_BY.getValue();
        public static final String PICKED_UP_BY = FieldName.PICKED_UP_BY.getValue();
        public static final String CREATED_AT = FieldName.CREATED_AT.getValue();
        public static final String PICKED_UP_AT = FieldName.PICKED_UP_AT.getValue();
        public static final String CLOSED_AT = FieldName.CLOSED_AT.getValue();
        public static final String CREATED_COMMENT = FieldName.CREATED_COMMENT.getValue();
        public static final String CLOSED_COMMENT = FieldName.CLOSED_COMMENT.getValue();
        public static final String LATITUDE = FieldName.LATITUDE.getValue();
        public static final String LONGITUDE = FieldName.LONGITUDE.getValue();
        public static final String CREATED_PICTURES = FieldName.CREATED_PICTURES.getValue();
        public static final String CLOSED_PICTURES = FieldName.CLOSED_PICTURES.getValue();
        public static final String POLLUTION_LEVEL = FieldName.CREATED_POLLUTION_LEVEL.getValue();
        public static final String CREATED_REPUTATION = FieldName.CREATED_REPUTATION.getValue();
        public static final String CLOSED_REPUTATION = FieldName.CLOSED_REPUTATION.getValue();
        public static final String CLOSED_BY = FieldName.CLOSED_BY.getValue();

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
                _ID,
                CREATED_BY,
                PICKED_UP_BY,
                CREATED_AT,
                PICKED_UP_AT,
                CLOSED_AT,
                CREATED_COMMENT,
                CLOSED_COMMENT,
                LATITUDE,
                LONGITUDE,
                CREATED_PICTURES,
                CLOSED_PICTURES,
                POLLUTION_LEVEL,
                CREATED_REPUTATION,
                CLOSED_REPUTATION,
                CLOSED_BY };

        // TODO: add column containing "timestamp" (latest activity time) and make default sort by this column
		/**
		 * The default sort order for queries containing NAME fields.
		 */
		public static final String SORT_ORDER_DEFAULT = CREATED_AT + " ASC";
	}


}
