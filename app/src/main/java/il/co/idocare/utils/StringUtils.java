package il.co.idocare.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO: move the methods of this class to the class that translates network schemes into entities
 */

public class StringUtils {
    
    public static @NonNull String listToCommaSeparatedString(@Nullable List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder("");
            for (int i=0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i < list.size()-1) sb.append(", ");
            }
            return sb.toString();
        }
    }

    public static @NonNull List<String> commaSeparatedStringToList(String string) {
        if (string == null || string.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            return Arrays.asList(string.split(", "));
        }
    }


}
