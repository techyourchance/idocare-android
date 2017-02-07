package il.co.idocare.contentproviders;


import java.util.Collections;
import java.util.List;

public final class ContentProviderUtils {
    private ContentProviderUtils() {}

    /**
     * Objects of this class bundle together selection clause and selection arguments
     */
    public static class SelectionAndSelectionArgsPair {
        private String mSelection;
        private String[] mSelectionArgs;

        public SelectionAndSelectionArgsPair(String selection, String[] selectionArgs) {
            mSelection = selection;
            mSelectionArgs = selectionArgs;
        }

        public String getSelection() {
            return mSelection;
        }

        public String[] getSelectionArgs() {
            return mSelectionArgs;
        }
    }

    /**
     * @throws IllegalArgumentException if the list of arguments is empty
     */
    public static SelectionAndSelectionArgsPair getSelectionByColumnForListOfValues(
            String columnName, List<String> args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("there is no semantically correct behavior for empty args");
        }

        StringBuilder sb = new StringBuilder(columnName);
        sb.append(" IN ( ");

        String[] selectionsArgs = new String[args.size()];

        for (int i = 0; i < args.size(); i++) {
            sb.append(" ? ");
            if (i < args.size() - 1) {
                sb.append(",");
            }
            selectionsArgs[i] = args.get(i);
        }

        sb.append(" ) ");
        String selection = sb.toString();

        return new SelectionAndSelectionArgsPair(selection, selectionsArgs);
    }


}
