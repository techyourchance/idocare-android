package il.co.idocare.requests;

import java.util.Comparator;

/**
 * This comparator can be used in order to compare {@link RequestEntity} by their IDs
 */
public class RequestsByIdComparator implements Comparator<RequestEntity> {
    @Override
    public int compare(RequestEntity lhs, RequestEntity rhs) {
        return lhs.getId().compareTo(rhs.getId());
    }
}
