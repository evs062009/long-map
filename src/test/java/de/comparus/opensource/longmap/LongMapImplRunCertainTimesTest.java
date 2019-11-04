package de.comparus.opensource.longmap;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LongMapImplRunCertainTimesTest {

    private static final int TEST_TIMES = 100;  //the number of cycles that all tests pass
    private static final int TEST_OBJECTS_SIZE = 27;    //the number of key-value mapping on which the map is tested

    private static Random random;
    private static Set<Long> testObjects;   //partially random set on which the map is tested
    private static LongMap<Long> map;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[TEST_TIMES][0];
    }

    @Test
    //has to be a first test
    //creates one set of test data for one loop of all tests
    public void $initTestLoop() {
        random = new Random();
        testObjects = getTestObjects();
        map = new LongMapImpl<>();
    }

    @Test
    public void test41PutReturnsCorrectValue() {
        //GIVEN
        //creating test objects in $initTestLoop
        for (Long expected : testObjects) {
            //WHEN
            Long actual = map.put(expected, expected);
            //THEN
            assertTrue(actual != null && actual.equals(expected));
        }
    }

    @Test(expected = NullPointerException.class)
    public void test44PutThrowsExceptionWithNullValue() {
        //WHEN
        map.put(0, null);
    }

    @Test
    public void test51SizeReturnsCorrectNumber() {
        //GIVEN
        //filling map in test41
        int expected = testObjects.size();
        //THEN
        assertEquals(expected, map.size());
    }

    @Test
    public void test52IsEmptyReturnFalseIfMapNotEmpty() {
        //GIVEN
        //filling map in test41
        //THEN
        assertFalse(map.isEmpty());
    }

    @Test
    public void test53ContainsKeyReturnsTrueWithExistingKey() {
        //GIVEN
        //filling map in test41
        //WHEN
        boolean actual = map.containsKey(-1);
        //THEN
        assertTrue(actual);
    }

    @Test
    public void test54ContainsKeyReturnsFalseWithNonexistentKey() {
        //GIVEN
        //filling map in test41
        //WHEN
        boolean actual = map.containsKey(0);
        //THEN
        assertFalse(actual);
    }

    @Test
    public void test55ContainsValueReturnsTrueWithExistingValue() {
        //GIVEN
        //filling map in test41
        //WHEN
        boolean actual = map.containsValue(-1L);
        //THEN
        assertTrue(actual);
    }

    @Test
    public void test56ContainsValueReturnsFalseWithNonexistentValue() {
        //GIVEN
        //filling map in test41
        //WHEN
        boolean actual = map.containsValue(0L);
        //THEN
        assertFalse(actual);
    }

    @Test
    public void test57ContainsValueReturnsFalseWithNullValue() {
        //GIVEN
        //filling map in test41
        //WHEN
        boolean actual = map.containsValue(null);
        //THEN
        assertFalse(actual);
    }

    @Test
    public void test61GetReturnsCorrectValue() {
        //GIVEN
        //filling map in test41
        for (Long expected : testObjects) {
            //WHEN
            Long actual = map.get(expected);
            //THEN
            assertEquals(expected, actual);
        }
    }

    @Test
    public void test62GetReturnsNullWithNonexistentKey() {
        //GIVEN
        //filling map in test41
        //WHEN
        Long actual = map.get(0);
        //THEN
        assertNull(actual);
    }

    @Test
    public void test63KeysReturnsCorrectSequence() {
        //GIVEN
        //filling map in test41
        //WHEN
        List<Long> actual = Arrays.stream(map.keys()).boxed().collect(Collectors.toList());
        //THEN
        assertTrue(actual.containsAll(testObjects));
    }

    @Test
    public void test64ValuesReturnsCorrectSequence() {
        //GIVEN
        //filling map in test41
        //WHEN
        List<Long> actual = Arrays.stream(map.values()).collect(Collectors.toList());
        //THEN
        assertTrue(actual.containsAll(testObjects));
    }

    @Test
    public void test71RemoveReturnsCorrectValue() {
        //GIVEN
        //filling map in test41
        long expected = 1;
        //WHEN
        long actual = map.remove(expected);
        //THEN
        assertEquals(expected, actual);
    }

    @Test
    public void test72GetNotFindRemovedElement() {
        //GIVEN
        //map with deleted element in test71
        Long expected = map.get(1);
        //THEN
        assertNull(expected);
    }

    @Test
    public void test73SizeReturnsCorrectNumberAfterRemoving() {
        //GIVEN
        //map with deleted in from test71
        int expected = testObjects.size() - 1;
        //WHEN
        int actual = (int) map.size();
        //THEN
        assertEquals(expected, actual);
    }

    @Test
    public void test74KeysReturnsCorrectSequenceAfterRemoving() {
        //GIVEN
        //map with deleted element in test71
        long deletedKey = 1;
        //WHEN
        List<Long> actual = Arrays.stream(map.keys()).boxed().collect(Collectors.toList());
        //THEN
        assertFalse(actual.contains(deletedKey));
    }

    @Test
    public void test82SizeZeroAfterClearing() {
        //GIVEN
        int expected = 0;
        //WHEN
        map.clear();
        int actual = (int) map.size();
        //THEN
        assertEquals(expected, actual);
    }

    @Test
    public void test83IsEmptyReturnsTrueAfterClearing() {
        //GIVEN
        //clearing map in test82
        //THEN
        assertTrue(map.isEmpty());
    }

    @Test
    public void test84KeysReturnsEmptyArrayAfterClearing() {
        //GIVEN
        //clearing map in test82
        int expected = 0;
        //WHEN
        int actual = map.keys().length;
        //THEN
        assertEquals(expected, actual);
    }

    @Test
    public void test85ValuesReturnsNullAfterClearing() {
        //GIVEN
        //clearing map in test82
        //THEN
        assertNull(map.values());
    }

    @Test
    public void test91PutOfExistingElementReplaceValue() {
        //GIVEN
        long key = 0;
        Long value = 1L;
        Long expected = 2L;
        map.put(key, value);
        //WHEN
        map.put(key, expected);
        Long actual = map.get(key);
        //THEN
        assertEquals(expected, actual);
    }

    private static Set<Long> getTestObjects() {
        int half = TEST_OBJECTS_SIZE / 2 - 1;
        LongStream stream = LongStream.of(-1L, 1L);
        LongStream positiveRandom = random.longs(half, 2, Long.MAX_VALUE);
        LongStream negativeRandom = random.longs(half, Long.MIN_VALUE, -1);
        return LongStream.concat(stream, LongStream.concat(positiveRandom, negativeRandom)).boxed()
                .collect(Collectors.toSet());
    }
}