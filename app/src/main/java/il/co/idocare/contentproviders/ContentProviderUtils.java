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
        return getSelectionByColumnInner(columnName, "IN", args);
    }


    /**
     * @throws IllegalArgumentException if the list of arguments is empty
     */
    public static SelectionAndSelectionArgsPair getSelectionByColumnExceptListOfValues(
            String columnName, List<String> args) {
        return getSelectionByColumnInner(columnName, "NOT IN", args);
    }

    private static SelectionAndSelectionArgsPair getSelectionByColumnInner(
            String columnName, String selectionOperator, List<String> args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("there is no semantically correct behavior for empty args");
        }

        StringBuilder sb = new StringBuilder(columnName); // column
        sb.append(" ").append(selectionOperator).append(" ( "); // selection operator

        String[] selectionsArgs = new String[args.size()];

        // selection placeholders and args
        for (int i = 0; i < args.size(); i++) {
            sb.append(" ? ");
            if (i < args.size() - 1) {
                sb.append(",");
            }
            selectionsArgs[i] = args.get(i);
        }

        sb.append(" ) "); // complete selection statement
        String selection = sb.toString();

        return new SelectionAndSelectionArgsPair(selection, selectionsArgs);
    }


}
