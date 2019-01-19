package il.co.idocare.utils;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class IdcDateTimeUtils {

    private static final DateTimeFormatter sFormatter =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private IdcDateTimeUtils() {}

    public static String getCurrentDateTimeLocalized() {
        DateTime dateTime = DateTime.now();
        return sFormatter.print(dateTime);
    }

    public static String getLatestDateTime(String... dateTimes) {
        DateTime latestDateTime = null;

        DateTime iteratedDateTime = null;
        for (int i = 0; i < dateTimes.length; i++) {
            if (dateTimes[i] != null) {
                iteratedDateTime = DateTime.parse(dateTimes[i], sFormatter);
                if (latestDateTime == null) {
                    latestDateTime = iteratedDateTime;
                } else if (iteratedDateTime.isAfter(latestDateTime)) {
                    latestDateTime = iteratedDateTime;
                }
            }
        }

        if (latestDateTime == null) {
            throw new IllegalArgumentException("at least one element of the array must a valid date");
        }

        return sFormatter.print(latestDateTime);
    }

    public static int compareDateTimes(String lhs, String rhs) {
        DateTime lhsDateTime = DateTime.parse(lhs, sFormatter);
        DateTime rhsDateTime = DateTime.parse(rhs, sFormatter);
        return lhsDateTime.compareTo(rhsDateTime);
    }
}
