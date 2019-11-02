package de.comparus.opensource.longmap;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class LongMapImpl<V> implements LongMap<V> {

    private Entry[] table;
    private Reserve<V> reserve;

    //FIXME in production needs to be private
    /*private*/ int capacity;
    /*private*/ int size;
    /*private*/ int reserveSize;
    //    private int maxLoop;
    private double topLoadFactor;
    private double bottomLoadFactor;

    private static final int DEFAULT_CAPACITY = 16;
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
    public LongMapImpl(int initialCapacity, int DEFAULT_MAX_LOOP, double topLoadFactor,
                       double bottomLoadFactor) {
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

        if (size <= MAX_CAPACITY) {
            if ((double) (size + 1) / capacity > topLoadFactor) {
                changeTableSize(true);
            }
            return putNewPair(key, value);
        }
        return null;
    }

    public V get(long key) {
        int index = getKeyIndex(key);
        if (index != -1) {
            return (V) getTable()[index].value;
        }
        return getFromReserve(key);
    }

    public V remove(long key) {
        V value;
        int index = getKeyIndex(key);
        if (index != -1) {
            value = removeEntry(index);
        } else {
            value = removeInReserve(key);
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
        return getKeyIndex(key) != -1
                || containsKeyInReserve(key);
    }

    public boolean containsValue(V value) throws NullPointerException {
        if (value == null) {
            throw new NullPointerException("value = null");
        }
        for (Entry entry : getTable()) {
            if (entry != null && value.equals(entry.value) && entry.zeroForDeletedEntry == 1) {
                return true;
            }
        }
        return containsValueInReserve(value);
    }

    public long[] keys() {
        if (size == 0) {
            return new long[0];
        } else {
            LongStream tableKeys = Arrays.stream(getTable()).filter(isEntryNotEmpty())
                    .mapToLong(Entry::getKey);
            LongStream reserveKeys = getReserveKeys();
            return LongStream.concat(tableKeys, reserveKeys).toArray();
//            return tableKeys.toArray();
        }
    }

    public V[] values() {
        if (size == 0) {
            return (V[]) new Object[0];
        } else {
            Stream tableValue = Arrays.stream(getTable()).filter(isEntryNotEmpty())
                    .map(Entry::getValue);
            Stream reserveValue = getReserveValues();
            return (V[]) Stream.concat(tableValue, reserveValue).toArray();
//            return (V[]) tableValue.toArray();
        }
    }

    public long size() {
        return size;
    }

    public void clear() {
        Arrays.fill(getTable(), null);
        capacity = DEFAULT_CAPACITY;
        table = null;
        clearReserve();
        size = 0;
    }

    private V putNewPair(long key, V value) {
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
        } else {
            putToReserve(key, value);
        }
        return value;
    }

    //FIXME package-private just for test, must be changed to private
    /*private*/ Entry[] getTable() {
        if (table == null) {
            table = new Entry[capacity];
        }
        return table;
    }

    private V removeEntry(int index) {
        Entry entry = getTable()[index];
        V value = (V) entry.value;
        entry.value = null;
        entry.zeroForDeletedEntry = 0;
        size--;
        return value;
    }

    private void changeTableSize(boolean increase) {
//        boolean isRehashed = false;
        if ((increase && capacity <= MAX_CAPACITY / 2)
                || (!increase && capacity >= DEFAULT_CAPACITY * 2)) {
//            int oldCapacity = capacity;
            capacity = (increase) ? (capacity << 1) : (capacity >> 1);
//            maxLoop = Math.min(capacity / 2, 20);
//            isRehashed = rehash();
//            capacity = isRehashed ? capacity : oldCapacity;
            rehash();
        }
    }

    private void rehash() {
        Entry[] newTable = new Entry[capacity];
        Stream<Entry> tableEntries = Arrays.stream(getTable()).filter(isEntryNotEmpty());
        Stream<Entry> reserveEntries = getReserveEntries();
        clearReserve();
        Stream.concat(tableEntries, reserveEntries).forEach(e -> putToTable(e, newTable));
        Arrays.fill(getTable(), null);
        table = newTable;
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

    private void putToTable(Entry entry, Entry[] newTable) {
//        for (int i = 0; i < maxLoop; i++) {
        for (int i = 0; i < DEFAULT_MAX_LOOP; i++) {
            int index = calculateIndex(entry.key, i);
            if (newTable[index] == null) {
                newTable[index] = entry;
                return;
            }
        }
        putToReserve(entry.key, (V) entry.value);
    }

    private Predicate<Entry> isEntryNotEmpty() {
        return e -> e != null && e.zeroForDeletedEntry == 1;
    }

    //FIXME package-private just for test, must be changed to private
    /*private*/ static class Entry<V> {
        //FIXME package-private just for test, must be changed to private
        /*private*/ long key;
        private V value;
        private byte zeroForDeletedEntry;         // == 0, when entry marked as deleted

        private Entry(long key, V value) {
            this.key = key;
            this.value = value;
            this.zeroForDeletedEntry = 1;
        }

        private Entry(Reserve<V> reserve) {
            this(reserve.key, reserve.value);
        }

        private long getKey() {
            return key;
        }

        private V getValue() {
            return value;
        }
    }

    //fixme in production has to be private
    /*private*/ static class Reserve<V> {

        //fixme in production has to be private
        /*private*/ long key;
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

    //fixme in production has to be private
    /*private*/ void putToReserve(long key, V value) {
        if (reserve == null) {
            reserve = new Reserve<>(key, value);
        } else {
            Reserve lastReserve = reserve;
            while (lastReserve.hasNext()) {
                lastReserve = lastReserve.next;
            }
            lastReserve.next = new Reserve<>(key, value);
        }
        size++;
        reserveSize++;
    }

    //fixme in production has to be private
    /*private*/ V getFromReserve(long key) {
        if (reserve != null) {
            Reserve lastReserve = reserve;
            while (true) {
                if (lastReserve.key == key) {
                    return (V) lastReserve.value;
                }
                if (lastReserve.hasNext()) {
                    lastReserve = lastReserve.next;
                } else {
                    break;
                }
            }
        }
        return null;
    }

    //fixme in production has to be private
    /*private*/ V removeInReserve(long key) {
        V value = null;
        if (reserve != null) {
            if (reserve.key == key) {
                value = reserve.value;
                reserve = null;
                size--;
            } else {
                Reserve lastReserve = reserve;
                while (lastReserve.hasNext()) {
                    if (lastReserve.next.key == key) {
                        value = removeNext(lastReserve);
                    }
                    lastReserve = lastReserve.next;
                }
            }
        }
        return value;
    }

    private V removeNext(Reserve element) {
        V value = (V) element.value;
        element.next = null;
        size--;
        return value;
    }

    //fixme in production has to be private
    /*private*/ boolean containsKeyInReserve(long key) {
        return getReserveKeys().anyMatch(k -> k == key);
    }

    //fixme in production has to be private
    /*private*/ boolean containsValueInReserve(V value) throws NullPointerException {
        if (value == null) {
            throw new NullPointerException("value = null");
        }
        return getReserveValues().anyMatch(value::equals);
    }

    //fixme in production has to be private
    /*private*/ LongStream getReserveKeys() {
        return getReserveStream().mapToLong(r -> r.key);
//        if (reserve != null) {
//            List<Long> keys = new ArrayList<>(Collections.singletonList(reserve.key));
//            Reserve lastReserve = reserve;
//            while (lastReserve.hasNext()) {
//                keys.add(lastReserve.next.key);
//                lastReserve = lastReserve.next;
//            }
//            return keys.stream().mapToLong(e -> e);
//        }
//        return LongStream.empty();
    }

    //fixme in production has to be private
    /*private*/ Stream<V> getReserveValues() {
        return getReserveStream().map(r -> r.value);
    }

    //fixme in production has to be private
    /*private*/ Stream<Entry> getReserveEntries() {
        return getReserveStream().map(Entry::new);
    }

    //fixme in production has to be private
    /*private*/ void clearReserve() {
        reserve = null;
        reserveSize = 0;
    }

    //fixme in production has to be private
    /*private*/ Stream<Reserve<V>> getReserveStream() {
        if (reserve == null) {
            return Stream.empty();
        }
        return Stream.iterate(reserve, r -> r.next).limit(reserveSize);
    }
}
