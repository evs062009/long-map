package de.comparus.opensource.longmap;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LongMapImplRunCertainTimesTest {

    private static final int TEST_TIMES = 100;
    private static final int TEST_OBJECTS_SIZE = 24;

    private static int initialCapacity;
    private static int defaultMaxLoop;
    private static double topLoadFactor;
    private static double bottomLoadFactor;

    static Random random;

    //    private static LongMap<Long> map;
    //FIXME just for my test
    private static LongMapImpl<Long> map;
    private static int maxCapacity;
    private static int maxReserveSize;
    //
    private static Set<Long> testObjects;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[TEST_TIMES][0];
    }

    @Test
    //has to be a first test
    //creates one set of test data for one loop of all tests
    public void $initTestLoop() {
        random = new Random();
        initialCapacity = 16;
        defaultMaxLoop = 10;
        topLoadFactor = 0.8;
        bottomLoadFactor = 0.0;
        map = new LongMapImpl<>(initialCapacity, defaultMaxLoop, topLoadFactor, bottomLoadFactor);
        testObjects = getTestObjects();
//        testObjects = get27TestObjects();
    }


    //fixme delete in production
    //test Objects Set---------------

//    @Test
//    public void test11ObjectsNotNull() {
//        assertNotNull(testObjects);
//    }
//
//    @Test
//    public void test12ObjectsNotEmpty() {
//        assertFalse(testObjects.isEmpty());
//    }
//
//    @Test
//    public void test13ObjectsHazZero() {
//        assertTrue(testObjects.contains(0L));
//    }
//
//    @Test
//    public void test14ObjectsHazPositive() {
//        assertTrue(testObjects.stream().anyMatch(e -> e > 0));
//    }
//
//    @Test
//    public void test15ObjectsHazNegative() {
//        assertTrue(testObjects.stream().anyMatch(e -> e < 0));
//    }
//
//    @Test
//    public void test16ObjectsHazLongPositive() {
//        assertTrue(testObjects.stream().anyMatch(e -> e > Integer.MAX_VALUE));
//    }
//
//    @Test
//    public void test17ObjectsHazLongNegative() {
//        assertTrue(testObjects.stream().anyMatch(e -> e < Integer.MIN_VALUE));
//    }

    //fixme delete in production?
    // test Reserve-------------------------------------

//    @Test
//    public void test21PutToReserveCorrectElementQuantity() {
//        //GIVEN
//        //test objects from $test
//        testObjects.forEach(e -> map.putToReserve(e, e));
//        //WHEN
//        long actual = map.getReserveStream().count();
//        //THEN
//        assertEquals(testObjects.size(), actual);
//    }

//    @Test
//    public void test22PutToReserveAllElements() {
//        //GIVEN
//        //filling map reserve from test21
//        //WHEN
//        List<Long> actual = map.getReserveStream().mapToLong(r -> r.key).boxed()
//                .collect(Collectors.toList());
//        //THEN
//        assertTrue(actual.containsAll(testObjects));
//    }

//    @Test
//    public void test23CorrectReserveSizeAfterPut() {
//        //GIVEN
//        //filling map reserve from test21
//        int expected = testObjects.size();
//        //WHEN
//        int actual = map.reserveSize;
//        //THEN
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    public void test24GetFromReserveReturnsCorrectElement() {
//        //GIVEN
//        //filling map reserve from test21
//        long key = testObjects.stream().findAny().get();
//        Long expected = key;
//        //WHEN
//        Long actual = map.getFromReserve(key);
//        //THEN
//        assertEquals(expected, actual);
//    }

//    @Test
//    public void test25GetReserveKeysReturnsCorrectData() {
//        //GIVEN
//        //filling map from test21
//        //WHEN
//        List<Long> actual = map.getReserveKeys().boxed().collect(Collectors.toList());
//        //THEN
//        assertTrue(actual.containsAll(testObjects) && actual.size() == testObjects.size());
//    }

//    @Test
//    public void test26GetReserveValuesReturnsCorrectData() {
//        //GIVEN
//        //filling map from test21
//        //WHEN
//        List<Long> actual = map.getReserveValues().collect(Collectors.toList());
//        //THEN
//        assertTrue(actual.containsAll(testObjects) && actual.size() == testObjects.size());
//    }

//    @Test
//    public void test27GetReserveEntriesReturnsCorrectData() {
//        //GIVEN
//        //filling map from test21
//        List<LongMapImpl.Entry> expected = testObjects.stream().map(e -> new LongMapImpl.Entry(e, e))
//                .collect(Collectors.toList());
//        //WHEN
//        List<LongMapImpl.Entry> actual = map.getReserveEntries().collect(Collectors.toList());
//        //THEN
//        assertTrue(actual.containsAll(expected) && actual.size() == expected.size());
//    }

//    @Test
//    public void test28RemoveInReserveDeletedElement() {
//        //GIVEN
//        //filling map reserve from test21
//        Iterator<Long> iterator = testObjects.iterator();
//        long l = testObjects.iterator().next();
//        for (int i = 0; i < random.nextInt(TEST_OBJECTS_SIZE); i++) {
//            l = iterator.next();
//        }
//
//        long key = l;
//        int expectedSize = testObjects.size() - 1;
//        Set<Long> expected = testObjects.stream().filter(e -> e != key).collect(Collectors.toSet());
//        //WHEN
//        map.removeInReserve(key);
//        List<Long> actual = map.getReserveKeys().boxed().collect(Collectors.toList());
//        //THEN
//        assertTrue("actual list does not match expected set", actual.containsAll(expected));
//        assertFalse("actual list contains deleted key", actual.contains(key));
//        assertEquals("actual size does not match expected size", actual.size(), expectedSize);
//    }
//
//    @Test
//    public void test29ClearReserveReallyCleared() {
//        //WHEN
//        map.clearReserve();
//        //THEN
//        assertTrue(map.reserve == null && map.reserveSize == 0
//                && map.getReserveStream().count() == 0);
//    }


    //------------------------


    @Test
    //needs calculateIndex() to have at least package-private access
    public void test31CalculateIndexMatchesTableSize() {
        for (long key : testObjects) {
            for (int i = 0; i < defaultMaxLoop; i++) {
                //WHEN
                int actual = map.calculateIndex(key, i);
                //THEN
                assertTrue(String.format("index %s (key %s, iteration %s) doesn`t match interval",
                        actual, key, i), actual >= 0 && actual < initialCapacity);
            }
        }
    }


    // ----------------------
    @Test

//    needs getTable() to have at least package-private access
//    needs Entry.key to have at least package-private access
    public void test41PutReturnsCorrectValue() {
        //GIVEN
        //test objects from $test
        //fixme delete in product
        if (map.reserveSize != 0) {
            fail("reserve is not empty");
        }

        //WHEN
        for (Long expected : testObjects) {
            Long actual = map.put(expected, expected);
            //THEN
            assertTrue(actual != null && actual.equals(expected));
        }
    }

    @Test
    public void test42PutAllElements() {
        //GIVEN
        //filling map from test41
        //WHEN
        List<Long> actual = Arrays.stream(map.getTable()).filter(Objects::nonNull).
                map(entry -> entry.key).collect(Collectors.toList());
        actual.addAll(map.getReserveKeys().boxed().collect(Collectors.toList()));

        //fixme delete in production
        if (maxCapacity < map.capacity) {
            maxCapacity = map.capacity;
        }
        if (maxReserveSize < map.maxReserveSize) {
            maxReserveSize = map.maxReserveSize;
        }

        //THEN
        assertTrue(actual.containsAll(testObjects));
    }

    @Test
    public void test43PutCorrectElementsQuantity() {
        //GIVEN
        //filling map from test41
        int expected = testObjects.size();
        //WHEN
        int actual = (int) Arrays.stream(map.getTable()).filter(Objects::nonNull).count();
        actual += map.getReserveStream().count();
        //THEN
        assertEquals(expected, actual);
    }

//    @Test
//    //needs table to have at least package-private access
//    public void putInsertedAllElementsWithRehash() {
//        //WHEN
//        for (Long obj : testObjects) {
//            map.put(obj, obj);
//        }
//        int mapSize = (int) Arrays.stream(map.table).filter(Objects::nonNull).count();
//        //THEN
//        Assert.assertTrue(Arrays.asList(map.table).containsAll(testObjects));
//        Assert.assertEquals(mapSize, testObjects.size());
//    }

    //test put of existing element without rehash

    //test put of existing element with rehash

//    @Test
//    public void get() {
//    }
//
//    @Test
//    public void remove() {
//    }
//
//    @Test
//    public void isEmpty() {
//    }
//
//    @Test
//    public void containsKey() {
//    }
//
//    @Test
//    public void containsValue() {
//    }
//
//    @Test
//    public void keys() {
//    }
//
//    @Test
//    public void values() {
//    }
//
//    @Test
//    public void size() {
//    }
//
//    @Test
//    public void clear() {
//    }

    //fixme delete
    @AfterClass
    public static void printInfo() {
        System.out.println("max capacity = " + maxCapacity);
        System.out.println("max reserve size = " + maxReserveSize);
    }

    private static Set<Long> get27TestObjects() {
        Set<Long> longs = random.longs(13, 1, 20).boxed()
                .collect(Collectors.toSet());
        addRandomsToTestObjects(longs, 13, -20, 0);
        longs.add(0L);
        return longs;
    }

    private static Set<Long> getTestObjects() {
        Set<Long> longs = random.longs((int) (TEST_OBJECTS_SIZE * 0.21),
                Integer.MAX_VALUE + 1L, Long.MAX_VALUE).boxed().collect(Collectors.toSet());
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.21), Long.MIN_VALUE,
                Integer.MIN_VALUE);
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.21), Short.MAX_VALUE + 1L,
                Integer.MAX_VALUE + 1L);
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.21), Integer.MIN_VALUE,
                Short.MIN_VALUE);
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.075), Byte.MAX_VALUE + 1,
                Short.MAX_VALUE + 1);
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.075), Short.MIN_VALUE,
                Byte.MIN_VALUE);
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.005), 1,
                Byte.MAX_VALUE + 1);
        addRandomsToTestObjects(longs, (int) (TEST_OBJECTS_SIZE * 0.005 - 1), Byte.MIN_VALUE, 0);
        longs.add(0L);
        return longs;
    }

    private static void addRandomsToTestObjects(Set<Long> longs, int quantity, long from, long to) {
        longs.addAll(random.longs(quantity, from, to).boxed().collect(Collectors.toList()));
    }
}