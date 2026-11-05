package pro.komaru.tridot.util.struct;

import pro.komaru.tridot.util.struct.func.Cons;
import pro.komaru.tridot.util.struct.func.Func;
import pro.komaru.tridot.util.struct.func.Prov;

/**
 * Ring pool of temporary objects for helpers called from many places. Single-threaded.
 *
 * <p>{@link #request(int)} reserves a block of slots that {@link #next()} hands out.
 * Nested {@link #take()} calls made while the block is active get slots after it,
 * so they can't overwrite it. Capacity is rounded up to a power of two.
 *
 * <p>Objects stay valid only until the ring advances by a full capacity.
 * Don't store them or return them past the current block.
 */
public class TempPool<T> {
    private final T[] pool;
    private final Cons<T> resetter;
    private final int mask;
    private int head;
    private int cursor;
    private int left;

    public TempPool(int capacity, Prov<T> initializer) {
        this(capacity, initializer, null);
    }

    @SuppressWarnings("unchecked")
    public TempPool(int capacity, Prov<T> initializer, Cons<T> resetter) {
        if (capacity <= 0 || capacity > 1 << 30) {
            throw new IllegalArgumentException("TempPool capacity must be in [1, 2^30], got (%s)".formatted(capacity));
        }
        int size = 1 << (32 - Integer.numberOfLeadingZeros(capacity - 1));
        this.pool = (T[]) new Object[size];
        this.mask = size - 1;
        this.resetter = resetter;
        for (int i = 0; i < size; i++) pool[i] = initializer.get();
    }

    public TempPool<T> request(int count) {
        if (count <= 0 || count > pool.length) {
            throw new IllegalArgumentException("Cannot request (%s) elements from TempPool: capacity is (%s)"
                    .formatted(count, pool.length));
        }
        if (left > 0) {
            throw new IllegalStateException("Cannot request (%s) elements from TempPool: (%s) still unconsumed"
                    .formatted(count, left));
        }
        cursor = head;
        head = (head + count) & mask;
        left = count;
        return this;
    }

    public T next() {
        return next(true);
    }

    public T next(boolean resetState) {
        if (left <= 0) {
            throw new IllegalStateException("Cannot retrieve next element from TempPool: no active request");
        }
        left--;
        T item = pool[cursor];
        cursor = (cursor + 1) & mask;
        return prepare(item, resetState);
    }

    public T take() {
        T item = pool[head];
        head = (head + 1) & mask;
        return prepare(item, true);
    }

    public <R> R use(Func<T, R> action) {
        return action.get(take());
    }

    public void reset() {
        left = 0;
    }

    private T prepare(T item, boolean resetState) {
        if (resetState && resetter != null) resetter.get(item);
        return item;
    }
}