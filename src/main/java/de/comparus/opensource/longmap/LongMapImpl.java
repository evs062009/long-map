package de.comparus.opensource.longmap;

import java.util.ArrayList;
import java.util.Arrays;

public class LongMapImpl<V> implements LongMap<V> {

    private int capacity;
    private Entry[] table;
    private int maxLoop;
    private int size;
    private double topLoadFactor;
    private double bottomLoadFactor;

    public LongMapImpl() {
        capacity = 4;
        table = new Entry[capacity];
        maxLoop = Math.min(table.length, 20);
        size = 0;
        topLoadFactor = 0.8;
        bottomLoadFactor = 0.6;
    }

    //XXX debug - decrease
    public V put(long key, V value) {
        int potentialIndex = -1;

        if (value == null) {
            throw new NullPointerException("value = null");
        }
        if ((double) size / capacity > topLoadFactor) {
            increaseTable();
        }

        for (int i = 0; i < maxLoop; i++) {
            int index = getIndex(key, i);

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
        return null;
    }

    public V get(long key) {
        int index = getIndex2(key);
        if (index != -1) {
            return (V) table[index].value;
        }
        return null;
    }

    public V remove(long key) {
        V value = null;
        int index = getIndex2(key);
        if (index != -1) {
            value = (V) table[index].value;
            table[index].value = null;
            table[index].zeroForDeletedEntry = 0;
            size--;

            if ((double) size / capacity < bottomLoadFactor) {
                decreaseTable();
            }
        }
        return value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {
        return getIndex2(key) != -1;
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
        return false;
    }

    public long[] keys() {
        if (size == 0) {
            return new long[0];
        } else {
            return Arrays.stream(table).filter(e -> e != null && e.zeroForDeletedEntry == 1)
                    .mapToLong(Entry::getKey).toArray();
        }
    }

    public V[] values() {
        if (size == 0) {
            return (V[]) new Object[0];
        } else {
            return (V[]) Arrays.stream(table).filter(e -> e != null && e.zeroForDeletedEntry == 1)
                    .map(Entry::getValue).toArray();
        }
    }

    public long size() {
        return size;
    }

    public void clear() {
        111
    }

    //XXX Correct method name
    private int getIndex2(long key) {
        for (int i = 0; i < maxLoop; i++) {
            int index = getIndex(key, i);
            if (table[index] != null && table[index].zeroForDeletedEntry == 1
                    && table[index].key == key) {
                return index;
            }
        }
        return -1;
    }

    private int getIndex(long key, int p) {

    }


    private static class Entry<V> {
        private long key;
        private V value;
        private byte zeroForDeletedEntry;         // == 0, when entry marked as deleted

        public Entry(long key, V value) {
            this.key = key;
            this.value = value;
            this.zeroForDeletedEntry = 1;
        }

        public long getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
