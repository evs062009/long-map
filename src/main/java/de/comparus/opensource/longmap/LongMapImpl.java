package de.comparus.opensource.longmap;

import com.sun.istack.internal.Nullable;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class LongMapImpl<V> implements LongMap<V> {

    private Entry[] table;

    private Reserve<V> reserve;
    private int capacity;
    private int size;
    private int reserveSize;

    private double topLoadFactor;
    private double bottomLoadFactor;

    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_CAPACITY = 1 << 30;
    private static final double DEFAULT_TOP_LOAD_FACTOR = 0.8;
    private static final double DEFAULT_BOTTOM_LOAD_FACTOR = 0.2;
    private static final int DEFAULT_MAX_LOOP = 10;

    /**
     * Creates long map instance with default parameters:
     * initial capacity = {@value LongMapImpl#DEFAULT_CAPACITY},
     * top load factor = {@value LongMapImpl#DEFAULT_TOP_LOAD_FACTOR},
     * bottom load factor = {@value LongMapImpl#DEFAULT_BOTTOM_LOAD_FACTOR}.
     * The table of the map is initialized lazily (not exists before the first usage).
     */
    public LongMapImpl() {
        capacity = DEFAULT_CAPACITY;
        size = 0;
        topLoadFactor = DEFAULT_TOP_LOAD_FACTOR;
        bottomLoadFactor = DEFAULT_BOTTOM_LOAD_FACTOR;
    }

    /**
     * Sets a mapping between specified key and specified value in the map.
     *
     * @param key   specified key for mapping.
     * @param value specified value for mapping.
     * @return the <tt>value</tt> if mapping has been done successfully, or {@code null} otherwise.
     * @throws NullPointerException if value == {@code null}.
     */
    @Nullable
    public V put(long key, V value) throws NullPointerException {
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

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key which mapping is searched in the map.
     * @return the <tt>value</tt> to which the specified key is mapped,
     * or {@code null} if there is no mapping with such key in the map.
     */
    @Nullable
    public V get(long key) {
        int index = getKeyIndex(key);
        if (index != -1) {
            return (V) getTable()[index].value;
        }
        return getFromReserve(key);
    }

    /**
     * Removes a mapping that associates with the specified key from the map.
     *
     * @param key the specified key whose mapping is deleted.
     * @return the <tt>value</tt> which was mapped with the specified key,
     * or {@code null} if there is no mapping for the specified key in the map.
     */
    @Nullable
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

    /**
     * Checks if there are no elements in the map.
     *
     * @return <tt>true</tt> if there are no elements in the map,
     * or <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks if there is a mapping for specified key in the map.
     *
     * @param key the key which mapping is searched in the map.
     * @return <tt>true</tt> if the map contains a mapping for the specified key,
     * or <tt>false</tt> otherwise.
     */
    public boolean containsKey(long key) {
        return getKeyIndex(key) != -1
                || containsKeyInReserve(key);
    }

    /**
     * Checks if there is a mapping with specified value in the map.
     *
     * @param value the value which mapping is searched in the map.
     * @return <tt>true</tt> if the map contains a mapping with the specified value,
     * or <tt>false</tt> if not or if value == null.
     */
    public boolean containsValue(V value) {
        if (value != null) {
            for (Entry entry : getTable()) {
                if (entry != null && value.equals(entry.value) && entry.zeroForDeletedEntry == 1) {
                    return true;
                }
            }
            return containsValueInReserve(value);
        }
        return false;
    }

    /**
     * Returns all keys mapped in the map.
     *
     * @return the <tt>array</tt> of all keys mapped in the map,
     * or empty array if the map is empty.
     */
    public long[] keys() {
        if (size == 0) {
            return new long[0];
        } else {
            LongStream tableKeys = Arrays.stream(getTable()).filter(isEntryNotEmpty())
                    .mapToLong(Entry::getKey);
            LongStream reserveKeys = getReserveKeys();
            return LongStream.concat(tableKeys, reserveKeys).toArray();
        }
    }

    /**
     * Returns all values mapped in the map.
     *
     * @return the <tt>array</tt> of all values mapped in the map.
     * or {@code null} if the map is empty.
     */
    @Nullable
    public V[] values() {
        if (size == 0) {
            return null;
        } else {
            Stream tableValue = Arrays.stream(getTable()).filter(isEntryNotEmpty())
                    .map(Entry::getValue);
            Stream reserveValue = getReserveValues();
            return (V[]) Stream.concat(tableValue, reserveValue).toArray();
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public long size() {
        return size;
    }

    /**
     * Removes all key-value mappings from the map.
     */
    public void clear() {
        Arrays.fill(getTable(), null);
        capacity = DEFAULT_CAPACITY;
        table = null;
        clearReserve();
        size = 0;
    }


    private V putNewPair(long key, V value) {
        int indexForInsert = -1;

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
            putToReserve(key, value, true);
        }
        return value;
    }

    private Entry[] getTable() {
        if (table == null) {
            table = new Entry[capacity];
        }
        return table;
    }

    private V removeEntry(int index) {
        Entry entry = getTable()[index];
        V value = (V) entry.value;
        entry.zeroForDeletedEntry = 0;
        size--;
        return value;
    }

    private void changeTableSize(boolean increase) {
        if ((increase && capacity <= MAX_CAPACITY / 2)
                || (!increase && capacity >= DEFAULT_CAPACITY * 2)) {
            capacity = (increase) ? (capacity << 1) : (capacity >> 1);
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

    private int calculateIndex(long key, int i) {
        int hash = getHash(key);
        int h1 = hash % capacity;
        int h2 = 1 + (hash % (capacity - 1));
        return (h1 + i * h2) % capacity;
    }

    private int getHash(long key) {
        return Math.abs((int) (key ^ (key >>> 32)));
    }

    private void putToTable(Entry entry, Entry[] newTable) {
        for (int i = 0; i < DEFAULT_MAX_LOOP; i++) {
            int index = calculateIndex(entry.key, i);
            if (newTable[index] == null) {
                newTable[index] = entry;
                return;
            }
        }
        putToReserve(entry.key, (V) entry.value, false);
    }

    private Predicate<Entry> isEntryNotEmpty() {
        return e -> e != null && e.zeroForDeletedEntry == 1;
    }

    private static class Entry<V> {
        private long key;
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

    private void putToReserve(long key, V value, boolean isSizeIncrease) {
        if (reserve == null) {
            reserve = new Reserve<>(key, value);
        } else {
            Reserve lastReserve = reserve;
            while (lastReserve.hasNext()) {
                lastReserve = lastReserve.next;
            }
            lastReserve.next = new Reserve<>(key, value);
        }
        if (isSizeIncrease) {
            size++;
        }
        reserveSize++;

        if (reserveSize > Math.max(10, size / 20)) {
            changeTableSize(true);
        }
    }

    @Nullable
    private V getFromReserve(long key) {
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

    @Nullable
    private V removeInReserve(long key) {
        V value = null;
        if (reserve != null) {
            if (reserve.key == key) {
                value = removeReserveFirstElement();
            } else {
                value = removeReserveMiddleElement(key, reserve);
            }
        }
        return value;
    }

    private V removeReserveFirstElement() {
        V value;
        value = reserve.value;
        if (reserve.next == null) {
            reserve = null;
            reserveSize = 0;
        } else {
            reserve = reserve.next;
            reserveSize--;
        }
        size--;
        return value;
    }

    @Nullable
    private V removeReserveMiddleElement(long key, Reserve current) {
        V value = null;
        while (current.hasNext()) {
            Reserve next = current.next;
            if (next.key == key) {
                value = (V) next.value;
                current.next = next.next;
                size--;
                reserveSize--;
                break;
            }
            current = next;
        }
        return value;
    }

    private boolean containsKeyInReserve(long key) {
        return getReserveKeys().anyMatch(k -> k == key);
    }

    private boolean containsValueInReserve(V value) {
        if (value != null) {
            return getReserveValues().anyMatch(value::equals);
        }
        return false;
    }

    private LongStream getReserveKeys() {
        return getReserveStream().mapToLong(r -> r.key);
    }

    private Stream<V> getReserveValues() {
        return getReserveStream().map(r -> r.value);
    }

    private Stream<Entry> getReserveEntries() {
        return getReserveStream().map(Entry::new);
    }

    private void clearReserve() {
        reserve = null;
        reserveSize = 0;
    }

    private Stream<Reserve<V>> getReserveStream() {
        if (reserve == null) {
            return Stream.empty();
        }
        return Stream.iterate(reserve, r -> r.next).limit(reserveSize);
    }
}
