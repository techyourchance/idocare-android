package il.co.idocare.requests;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class RequestsByIdComparatorTest {

    private RequestsByIdComparator SUT;

    @Before
    public void setup() throws Exception {
        SUT = new RequestsByIdComparator();
    }

    @Test
    public void compare_returnsCorrectValues() throws Exception {
        // Arrange
        RequestEntity entity1 = RequestEntity.getBuilder().setId("10").build();
        RequestEntity entity2 = RequestEntity.getBuilder().setId("20").build();
        RequestEntity entity3 = RequestEntity.getBuilder().setId("30").build();
        // Act
        int result1 = SUT.compare(entity1, entity2);
        int result2 = SUT.compare(entity2, entity3);
        int result3 = SUT.compare(entity3, entity1);
        int result4 = SUT.compare(entity1, entity1);
        // Assert
        assertTrue(result1 < 0);
        assertTrue(result2 < 0);
        assertTrue(result3 > 0);
        assertTrue(result4 == 0);
    }
}