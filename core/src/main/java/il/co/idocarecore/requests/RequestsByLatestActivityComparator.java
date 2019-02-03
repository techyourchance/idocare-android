package il.co.idocarecore.requests;


import java.util.Comparator;

import il.co.idocarecore.utils.IdcDateTimeUtils;
import il.co.idocarecore.utils.IdcDateTimeUtils;

public class RequestsByLatestActivityComparator implements Comparator<RequestEntity> {

    @Override
    public int compare(RequestEntity lhs, RequestEntity rhs) {
        // reverting arguments order for latest to earliest sort
        return IdcDateTimeUtils.compareDateTimes(rhs.getLatestActivityAt(), lhs.getLatestActivityAt());
    }
}
