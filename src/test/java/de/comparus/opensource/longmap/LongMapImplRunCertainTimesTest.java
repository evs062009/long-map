package de.comparus.opensource.longmap;

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

    private static final int TEST_TIMES = 30;
    private static final int TEST_OBJECTS_SIZE = 1000;

    private static int initialCapacity;
    private static int defaultMaxLoop;
        private static double topLoadFactor;
    private static double bottomLoadFactor;

    //    private static LongMap<Long> map;
    //FIXME just for my test
    private static LongMapImpl<Long> map;
    private static Set<Long> testObjects;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[TEST_TIMES][0];
    }

//    @Before
//    public void setUp() {
//        initialCapacity = 16;
//        defaultMaxLoop = 10;
//        topLoadFactor = 0.4;
//        bottomLoadFactor = 0.0;
//        map = new LongMapImpl<>(initialCapacity, defaultMaxLoop, topLoadFactor, bottomLoadFactor);
//    }
//
//    @After
//    public void tearDown() {
//        map.clear();
//        map = null;
//    }

    @Test
    //creates one set of test data for one loop of all tests
    public void $initTestLoop() {
        initialCapacity = 16;
        defaultMaxLoop = 20;
        topLoadFactor = 0.8;
        bottomLoadFactor = 0.0;
        map = new LongMapImpl<>(initialCapacity, defaultMaxLoop, topLoadFactor, bottomLoadFactor);
        testObjects = getTestObjects();
    }

    //fixme delete in production
    //-----------------------------
    @Test
    public void testObjectsNotNull() {
        assertNotNull(testObjects);
    }

    @Test
    public void testObjectsNotEmpty() {
        assertFalse(testObjects.isEmpty());
    }

    @Test
    public void testObjectsHazZero() {
        assertTrue(testObjects.contains(0L));
    }

    @Test
    public void testObjectsHazPositive() {
        assertTrue(testObjects.stream().anyMatch(e -> e > 0));
    }

    @Test
    public void testObjectsHazNegative() {
        assertTrue(testObjects.stream().anyMatch(e -> e < 0));
    }

    @Test
    public void testObjectsHazLongPositive() {
        assertTrue(testObjects.stream().anyMatch(e -> e > Integer.MAX_VALUE));
    }

    @Test
    public void testObjectsHazLongNegative() {
        assertTrue(testObjects.stream().anyMatch(e -> e < Integer.MIN_VALUE));
    }
    //------------------------


    @Test
    //needs calculateIndex() to have at least package-private access
    public void calculateIndexMatchesTableSize() {
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

    @Test
//    needs getTable() to have at least package-private access
//    needs Entry.key to have at least package-private access
    public void putAllElements() {
        //WHEN
        testObjects.forEach(e -> map.put(e, e));
        List<Long> actual = Arrays.stream(map.getTable()).filter(Objects::nonNull).map(entry -> entry.key)
                .collect(Collectors.toList());

        ///fixme delete
        System.out.println("capacity = " + map.capacity);
        //THEN
        assertTrue(actual.containsAll(testObjects));
    }

    @Test
//    needs getTable() to have at least package-private access
    public void putCorrectElementsQuantity() {
        //WHEN
        testObjects.forEach(e -> map.put(e, e));
        int mapSize = (int) Arrays.stream(map.getTable()).filter(Objects::nonNull).count();
        //THEN
        assertEquals(String.format("Put %s, in table %s", testObjects.size(), mapSize), mapSize,
                testObjects.size());
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

    private static Set<Long> getTestObjects() {
        Random random = new Random();

        Set<Long> longList = random.longs((int) (TEST_OBJECTS_SIZE * 0.21), Integer.MAX_VALUE + 1L,
                Long.MAX_VALUE).boxed().collect(Collectors.toSet());
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.21), Long.MIN_VALUE, Integer.MIN_VALUE, random);
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.21), Short.MAX_VALUE + 1L,
                Integer.MAX_VALUE + 1L, random);
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.21), Integer.MIN_VALUE, Short.MIN_VALUE, random);
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.075), Byte.MAX_VALUE + 1,
                Short.MAX_VALUE + 1, random);
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.075), Short.MIN_VALUE, Byte.MIN_VALUE, random);
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.005), 1, Byte.MAX_VALUE + 1, random);
        addRandomsToTestObjects(longList, (int) (TEST_OBJECTS_SIZE * 0.005 - 1), Byte.MIN_VALUE, 0, random);
        longList.add(0L);
        return longList;
    }

    private static void addRandomsToTestObjects(Set<Long> longList, int quantity, long from, long to, Random random) {
        longList.addAll(random.longs(quantity, from, to).boxed().collect(Collectors.toList()));
    }

//    private Set<Long> getSimpleTestObjects() {
//        return LongStream.rangeClosed(0, capacity / 2).boxed().collect(Collectors.toSet());
//    }
}