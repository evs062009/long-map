package de.comparus.opensource.longmap;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class LongMapImpl<V> implements LongMap<V> {

    private Entry[] table;
    private int capacity;
    private int size;
    private int reserveSize;
    private int maxLoop;
    private double topLoadFactor;
    private double bottomLoadFactor;

    private static final int DEFAULT_CAPACITY = 8;
    private static final int MAX_CAPACITY = 1 << 30;

    public LongMapImpl() {
        capacity = DEFAULT_CAPACITY;
        table = new Entry[capacity];
        maxLoop = Math.min(table.length, 20);
        size = 0;
        topLoadFactor = 0.9;
        bottomLoadFactor = 0.4;
    }

    public V put(long key, V value) {
        if (value == null) {
            throw new NullPointerException("value = null");
        }
        if ((double) (size + 1) / capacity > topLoadFactor) {
            increaseTable();
        }

        return putNewPair(key, value);
    }

    public V get(long key) {
        int index = getKeyIndex(key);
        if (index != -1) {
            return (V) table[index].value;
        }
        return getFromReserve(key);
    }

    public V remove(long key) {
        V value;
        int index = getKeyIndex(key);
        if (index != -1) {
            value = remove(index);
        } else {
            value = removeInReserve(key);
        }
        if ((double) size / capacity < bottomLoadFactor) {
            decreaseTable();
        }
        return value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        return getKeyIndex(key) != -1 || containsKeyInReserve();
    }

    public boolean containsValue(V value) {
        if (value == null) {
            throw new NullPointerException("value = null");
        }
        for (Entry entry : table) {
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
            LongStream tableKeys = Arrays.stream(table).filter(isEntryNotEmpty()).mapToLong(Entry::getKey);
            LongStream reserveKeys = getReserveKeys();
            return LongStream.concat(tableKeys, reserveKeys).toArray();
        }
    }

    public V[] values() {
        if (size == 0) {
            return (V[]) new Object[0];
        } else {
            return (V[]) Arrays.stream(table).filter(isEntryNotEmpty()).map(Entry::getValue).toArray();
            111
        }
    }

    public long size() {
        return size;
    }

    public void clear() {
        clearTable();
        capacity = DEFAULT_CAPACITY;
        table = new Entry[capacity];
        clearReserve();
        size = 0;
    }


    private V putNewPair(long key, V value) {
        int potentialIndex = -1;

        for (int i = 0; i < maxLoop; i++) {
            int index = calculateIndex(key, i);

            if (table[index] == null) {
                if (potentialIndex == -1) {
                    potentialIndex = index;
                }
            } else {
                if (table[index].zeroForDeletedEntry == 0) {
                    if (potentialIndex == -1) {
                        potentialIndex = index;
                    }
                } else if (table[index].key == key) {
                    table[index].value = value;
                    return value;
                }
            }
        }

        if (potentialIndex != -1) {
            table[potentialIndex] = new Entry<>(key, value);
            size++;
            return value;
        }
        return putToReserve(key, value);
    }

    private V remove(int index) {
        V value = (V) table[index].value;
        table[index].value = null;
        table[index].zeroForDeletedEntry = 0;
        size--;
        return value;
    }

    private void increaseTable() {
        if (capacity < MAX_CAPACITY) {
            capacity = capacity << 1;
            reHash();
        }
    }

    private void decreaseTable() {
        if (capacity > DEFAULT_CAPACITY) {
            capacity = capacity >> 1;
            reHash();
        }
    }

    private void reHash() {
        Entry[] newTable = new Entry[capacity];
        Arrays.stream(table).filter(isEntryNotEmpty()).forEach(e -> putForRehash(e, newTable));
        clearTable();
        table = newTable;
    }

    private int getKeyIndex(long key) {
        for (int i = 0; i < maxLoop; i++) {
            int index = calculateIndex(key, i);
            if (table[index] != null && table[index].zeroForDeletedEntry == 1
                    && table[index].key == key) {
                return index;
            }
        }
        return -1;
    }

    private int calculateIndex(long key, int i) {
        int hash = getHash(key);
        int h1 = hash % capacity;
        int h2 = 1 + (hash % (capacity - 1));
        return (h1 + i * h2) % 13;
    }

    private int getHash(long key) {
        return (int) (key ^ (key >>> 32));
    }

    private void putForRehash(Entry entry, Entry[] table) {
        for (int i = 0; i < maxLoop; i++) {
            int index = calculateIndex(entry.key, i);
            if (table[index] == null || table[index].zeroForDeletedEntry == 0) {
                table[index] = entry;
                return;
            }
        }
        if (putToReserve(entry.key, (V) entry.value) == null) {
            throw new RuntimeException("Error in rehash.");
        }
    }

    private V putToReserve(long key, V value) {
        111
    }

    private void clearTable() {
        for (int i = 0; i < capacity; i++) {
            table[i] = null;
        }
    }

    private Predicate<Entry> isEntryNotEmpty() {
        return e -> e != null && e.zeroForDeletedEntry == 1;
    }

    private V getFromReserve(long key) {
    }

    private V removeInReserve(long key) {
    }

    private boolean containsKeyInReserve() {
    }

    private boolean containsValueInReserve(V value) {
    }

    private LongStream getReserveKeys() {

    }

    private void clearReserve() {
        111
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

        private long getKey() {
            return key;
        }

        private V getValue() {
            return value;
        }
    }
}
