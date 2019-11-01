package de.comparus.opensource.longmap;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class LongMapImpl<V> implements LongMap<V> {

    private Entry[] table;
    private Reserve<V> reserve;

    //FIXME in production needs to be private
    /*private*/ int capacity;
    private int size;               //include reserve size
    private int reserveSize;
    //    private int maxLoop;
    private double topLoadFactor;
    private double bottomLoadFactor;

    private static final int DEFAULT_CAPACITY = 16;
    private static final int RESERVE_CAPACITY = 10;
    private static final int MAX_CAPACITY = 1 << 30;

    //FIXME in production needs to be private static final
    /*private static final*/ int DEFAULT_MAX_LOOP = 10;

    public LongMapImpl() {
        capacity = DEFAULT_CAPACITY;
//        maxLoop = Math.min(capacity / 2, DEFAULT_MAX_LOOP);
        size = 0;
        topLoadFactor = 0.9;
        bottomLoadFactor = 0.4;
    }

    //FIXME just for test
    public LongMapImpl(int initialCapacity, int DEFAULT_MAX_LOOP, double topLoadFactor, double bottomLoadFactor) {
//    public LongMapImpl(int initialCapacity, int DEFAULT_MAX_LOOP, double bottomLoadFactor) {
        capacity = initialCapacity;
        size = 0;
        this.DEFAULT_MAX_LOOP = DEFAULT_MAX_LOOP;
//        this.maxLoop = Math.min(capacity / 2, DEFAULT_MAX_LOOP);
        this.topLoadFactor = topLoadFactor;
        this.bottomLoadFactor = bottomLoadFactor;
    }

    public V put(long key, V value) {
        if (value == null) {
            throw new NullPointerException("value = null");
        }

        if ((double) (size + 1) / capacity > topLoadFactor) {
            changeTableSize(true);
        }
        boolean isChangeTableSize = false;
        for (int i = 0; i < 2; i++) {
            if (putNewPair(key, value)) {
                return value;
            }
            if (isChangeTableSize || !changeTableSize(true)) {
                break;
            }
            isChangeTableSize = true;
        }
        return null;
    }

    public V get(long key) {
        int index = getKeyIndex(key);
        if (index != -1 && getTable()[index] != null) {
            return (V) getTable()[index].value;
        }
//        return getFromReserve(key);
        return null;
    }

    public V remove(long key) {
        V value = null;
        int index = getKeyIndex(key);
        if (index != -1) {
            value = removeEntry(index);
//        } else {
//            value = removeInReserve(key);
        }
        if (size == 0) {
            clear();
        } else if ((double) size / capacity < bottomLoadFactor) {
            changeTableSize(false);
        }
        return value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        return getKeyIndex(key) != -1;// || containsKeyInReserve();
    }

    public boolean containsValue(V value) {
        if (value == null) {
            throw new NullPointerException("value = null");
        }
        for (Entry entry : getTable()) {
            if (entry != null && value.equals(entry.value) && entry.zeroForDeletedEntry == 1) {
                return true;
            }
        }
//        return containsValueInReserve(value);
        return false;
    }

    public long[] keys() {
        if (size == 0) {
            return new long[0];
        } else {
            LongStream tableKeys = Arrays.stream(getTable()).filter(isEntryNotEmpty()).mapToLong(Entry::getKey);
//            LongStream reserveKeys = getReserveKeys();
//            return LongStream.concat(tableKeys, reserveKeys).toArray();
            return tableKeys.toArray();
        }
    }

    public V[] values() {
        if (size == 0) {
            return (V[]) new Object[0];
        } else {
            Stream tableValue = Arrays.stream(getTable()).filter(isEntryNotEmpty()).map(Entry::getValue);
//            Stream reserveValue = getReserveValues();
//            return (V[]) Stream.concat(tableValue, reserveValue).toArray();
            return (V[]) tableValue.toArray();
        }
    }

    public long size() {
        return size;
    }

    public void clear() {
        Arrays.fill(getTable(), null);
        capacity = DEFAULT_CAPACITY;
        table = null;
//        clearReserve();
        size = 0;
    }


    private boolean putNewPair(long key, V value) {
        int indexForInsert = -1;

//        for (int i = 0; i < maxLoop; i++) {
        for (int i = 0; i < DEFAULT_MAX_LOOP; i++) {
            int index = calculateIndex(key, i);
            Entry entry = getTable()[index];

            if (entry == null || (entry.key == key && entry.zeroForDeletedEntry == 1)) {
                indexForInsert = index;
                break;
            }
            if (entry.zeroForDeletedEntry == 0 && indexForInsert == -1) {
                indexForInsert = index;
            }
        }

        if (indexForInsert != -1) {
            getTable()[indexForInsert] = new Entry<>(key, value);
            size++;
            return true;
        }
        return putToReserve(key, value);
//        return false;
    }

    //FIXME package-private just for test, must be changed to private
    /*private*/ Entry[] getTable() {
        if (table == null) {
            table = new Entry[capacity];
        }
        return table;
    }

    private Entry[] getReserve() {
        if (reserve == null) {
            reserve = new Entry[RESERVE_CAPACITY];
        }
        return reserve;
    }

    private V removeEntry(int index) {
        Entry entry = getTable()[index];
        V value = (V) entry.value;
        entry.value = null;
        entry.zeroForDeletedEntry = 0;
        size--;
        return value;
    }

    private boolean changeTableSize(boolean increase) {
        boolean isRehashed = false;
        if ((increase && capacity < MAX_CAPACITY) || (!increase && capacity > DEFAULT_CAPACITY)) {
            int oldCapacity = capacity;
            capacity = (increase) ? (capacity << 1) : (capacity >> 1);
//            maxLoop = Math.min(capacity / 2, 20);
            isRehashed = rehash();
            capacity = isRehashed ? capacity : oldCapacity;
        }
        return isRehashed;
    }

//    private void decreaseTable() {
//        if (capacity > DEFAULT_CAPACITY) {
//            capacity = capacity >> 1;
//            rehash();
//        }
//    }

    private boolean rehash() {
        Entry[] newTable = new Entry[capacity];
        List<Entry> entries = Arrays.stream(getTable()).filter(isEntryNotEmpty()).collect(Collectors.toList());
//        Stream<Entry> reserveEntries = getReserveEntries();
//        Stream.concat(tableEntries, reserveEntries).forEach(e -> putForRehash(e, newTable));
        for (Entry entry : entries) {
            if (!putToTable(entry, newTable)) {
                return false;
            }
        }
        Arrays.fill(getTable(), null);
//        clearReserve();
        table = newTable;
        return true;
    }

    private int getKeyIndex(long key) {
//        for (int i = 0; i < maxLoop; i++) {
        for (int i = 0; i < DEFAULT_MAX_LOOP; i++) {
            int index = calculateIndex(key, i);
            Entry entry = getTable()[index];
            if (entry != null) {
                if (entry.key == key && entry.zeroForDeletedEntry == 1) {
                    return index;
                }
            } else {
                return -1;
            }
        }
        return -1;
    }

    //FIXME public just for testing, must be changed to private
    /*private*/ int calculateIndex(long key, int i) {
        int hash = getHash(key);
        int h1 = hash % capacity;
        int h2 = 1 + (hash % (capacity - 1));
        return (h1 + i * h2) % capacity;
    }

    private int getHash(long key) {
        return Math.abs((int) (key ^ (key >>> 32)));
    }

    private boolean putToTable(Entry entry, Entry[] newTable) {
//        for (int i = 0; i < maxLoop; i++) {
        for (int i = 0; i < DEFAULT_MAX_LOOP; i++) {
            int index = calculateIndex(entry.key, i);
            if (newTable[index] == null) {
                newTable[index] = entry;
                return true;
            }
        }
//        if (putToReserve(entry.key, (V) entry.value) == null) {
//            throw new RuntimeException("Error in rehash.");
//        }
        return false;
    }

    private Predicate<Entry> isEntryNotEmpty() {
        return e -> e != null && e.zeroForDeletedEntry == 1;
    }

    private boolean putToReserve(long key, V value) {
        if (reserveSize > 10 && capacity >= MAX_CAPACITY) {
            return false;
        }

        for (int i = 0; i < getReserve().length; i++) {
            Entry entry = getReserve()[i];
            if (entry == null || entry.zeroForDeletedEntry == 0) {
                entry = new Entry(key, value);
                size++;
                reserveSize++;
                return true;
            }
        }
        return false;
    }

//    private V getFromReserve(long key) {
//        if (reserve == null) {
//            return null;
//        }
//        Reserve currentReserve = reserve;
//        while (currentReserve.hasNext()) {
//
//        }
//    }

//    private V removeInReserve(long key) {
//    }
//
//    private boolean containsKeyInReserve() {
//    }
//
//    private boolean containsValueInReserve(V value) {
//    }
//
//    private LongStream getReserveKeys() {
//
//    }
//
//    private Stream getReserveValues() {
//    }
//
//    private Stream<Entry> getReserveEntries() {
//    }
//
//    private void clearReserve() {
//        111
//    }

    //FIXME package-private just for test, must be changed to private
    static class Entry<V> {
        //FIXME package-private just for test, must be changed to private
        /*private*/ long key;
        private V value;
        private byte zeroForDeletedEntry;         // == 0, when entry marked as deleted

        private Entry(long key, V value) {
            this.key = key;
            this.value = value;
            this.zeroForDeletedEntry = 1;
        }

        private long getKey() {
            return key;
        }

        private V getValue() {
            return value;
        }
    }

    private static class Reserve<V> {
        private long key;
        private V value;
        private Reserve next;

        private Reserve(long key, V value) {
            this.key = key;
            this.value = value;
        }

        private boolean hasNext() {
            return next != null;
        }
    }
}
