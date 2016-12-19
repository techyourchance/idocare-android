package il.co.idocare.testdoubles.entities;

import java.util.Arrays;
import java.util.List;

import il.co.idocare.requests.RequestEntity;

/**
 * This class provides {@link RequestEntity} for tests
 */

public class RequestEntityProvider {

    public static final String NEW_REQUEST_ENTITY_ID = "10";
    public static final String PICKED_UP_REQUEST_ENTITY_ID = "50";
    public static final String CLOSED_REQUEST_ENTITY_ID = "80";


    public static final String REQUEST_CREATED_BY = "4";
    public static final String REQUEST_CREATED_AT = "2016-12-10T16:15:54Z";
    public static final String REQUEST_CREATED_COMMENT = "created comment";
    public static final List<String> REQUEST_CREATED_PICTURES = Arrays.asList("picture1", "picture2");
    public static final int REQUEST_CREATED_VOTES = 5;
    public static final double REQUEST_LATITUDE = 31.771959;
    public static final double REQUEST_LONGITUDE = 35.217018;
    public static final String REQUEST_PICKED_UP_BY = "30";
    public static final String REQUEST_PICKED_UP_AT = "2016-12-11T13:10:54Z";
    public static final String REQUEST_CLOSED_BY = REQUEST_PICKED_UP_BY;
    public static final String REQUEST_CLOSED_AT = "2016-12-12T13:10:01Z";
    public static final String REQUEST_CLOSED_COMMENT = "closed comment";
    public static final List<String> REQUEST_CLOSED_PICTURES = Arrays.asList("picture3", "picture4");
    public static final int REQUEST_CLOSED_VOTES = 1;
    public static final String REQUEST_LOCATION = "Jerusalem, Israel";


    public static RequestEntity getNewRequestEntity() {
        RequestEntity request = new RequestEntity(
                NEW_REQUEST_ENTITY_ID,
                REQUEST_CREATED_BY,
                REQUEST_CREATED_AT,
                REQUEST_CREATED_COMMENT,
                REQUEST_CREATED_PICTURES,
                REQUEST_CREATED_VOTES,
                REQUEST_LATITUDE,
                REQUEST_LONGITUDE,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                REQUEST_LOCATION);
        return request;
    }

    public static RequestEntity getPickedUpRequestEntity() {
        RequestEntity request = new RequestEntity(
                PICKED_UP_REQUEST_ENTITY_ID,
                REQUEST_CREATED_BY,
                REQUEST_CREATED_AT,
                REQUEST_CREATED_COMMENT,
                REQUEST_CREATED_PICTURES,
                REQUEST_CREATED_VOTES,
                REQUEST_LATITUDE,
                REQUEST_LONGITUDE,
                REQUEST_PICKED_UP_BY,
                REQUEST_PICKED_UP_AT,
                null,
                null,
                null,
                null,
                0,
                REQUEST_LOCATION);
        return request;
    }

    public static RequestEntity getClosedRequestEntity() {
        RequestEntity request = new RequestEntity(
                CLOSED_REQUEST_ENTITY_ID,
                REQUEST_CREATED_BY,
                REQUEST_CREATED_AT,
                REQUEST_CREATED_COMMENT,
                REQUEST_CREATED_PICTURES,
                REQUEST_CREATED_VOTES,
                REQUEST_LATITUDE,
                REQUEST_LONGITUDE,
                REQUEST_PICKED_UP_BY,
                REQUEST_PICKED_UP_AT,
                REQUEST_CLOSED_BY,
                REQUEST_CLOSED_AT,
                REQUEST_CLOSED_COMMENT,
                REQUEST_CLOSED_PICTURES,
                REQUEST_CLOSED_VOTES,
                REQUEST_LOCATION);
        return request;
    }
}
