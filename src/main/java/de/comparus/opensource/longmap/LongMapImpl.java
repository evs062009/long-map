package de.comparus.opensource.longmap;

public class LongMapImpl<V> implements LongMap<V> {

    private int capacity;
    private Entry[] table;
    private int maxLoop;
    private long size;

    public LongMapImpl() {
        capacity = 4;
        table = new Entry[capacity];
        maxLoop = Math.min(table.length, 20);
        size = 0;
    }

    public V put(long key, V value) {
        int potentialIndex = -1;
        for (int i = 0; i < maxLoop; i++) {
            int index = getIndex(i);

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
                }
            }
        }

        size++;
    }

    public V get(long key) {

    }

    public V remove(long key) {

        size--;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(long key) {

    }

    public boolean containsValue(V value) {

    }

    public long[] keys() {

    }

    public V[] values() {

    }

    public long size() {
        return size;
    }

    public void clear() {
        111
    }

    private int getIndex(int p) {

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
    }
}
