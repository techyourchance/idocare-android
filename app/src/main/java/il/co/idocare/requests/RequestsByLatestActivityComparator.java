package il.co.idocare.requests;


import java.util.Comparator;

import il.co.idocare.utils.IdcDateTimeUtils;

public class RequestsByLatestActivityComparator implements Comparator<RequestEntity> {

    @Override
    public int compare(RequestEntity lhs, RequestEntity rhs) {
        // reverting arguments order for latest to earliest sort
        return IdcDateTimeUtils.compareDateTimes(rhs.getLatestActivityAt(), lhs.getLatestActivityAt());
    }
}
