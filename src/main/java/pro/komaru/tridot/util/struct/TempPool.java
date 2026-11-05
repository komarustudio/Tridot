package pro.komaru.tridot.util.struct;

import pro.komaru.tridot.util.struct.func.Cons;
import pro.komaru.tridot.util.struct.func.Func;
import pro.komaru.tridot.util.struct.func.Prov;

import java.util.function.Function;

/**
 * A temporary object pool (scratchpad buffer) designed to provide garbage-free
 * temporary instances (such as vectors, matrices, etc.) during frame calculations
 * or intensive math operations.
 *
 * <p>It uses a circular buffer strategy combined with an explicit request-contract
 * mechanism to prevent state pollution and catch buffer overruns early.
 *
 * @param <T> the type of elements managed by this pool
 */
public class TempPool<T> {
    private final T[] pool;
    private int current = 0;
    private int available = 0;

    /**
     * Constructs a new {@code TempPool} with a fixed capacity and an element initializer.
     *
     * @param capacity    the maximum number of elements stored in the pool
     * @param initializer a provider/supplier function used to instantiate each element
     */
    @SuppressWarnings("unchecked")
    public TempPool(int capacity, Prov<T> initializer) {
        pool = (T[]) new Object[capacity];

        for (int i = 0; i < capacity; i++) {
            pool[i] = initializer.get();
        }
    }

    /**
     * Safely borrows 1 temporary object for a single operation,
     * automatically executing the action and returning the result.
     */
    public <R> R use(Func<T, R> action) {
        T item = take();
        return action.get(item);
    }

    /**
     * Requests 1 object and returns it.
     */
    public T take() {
        request(1);
        return next();
    }

    /**
     * Requests a specific number of elements for the upcoming operation or block of code.
     * <p>
     * This acts as a safety barrier: you must declare how many temporary objects
     * you intend to use before calling {@link #next()}.
     *
     * @endcode
     *
     * @param count the number of elements required (must be {@code <= pool.length})
     * @throws IllegalArgumentException if {@code count} exceeds the total pool capacity
     * @throws IllegalStateException    if the previous session left unconsumed elements
     *                                  ({@code available > 0}), indicating a broken contract
     */
    public void request(int count) {
        if (count > pool.length) {
            throw new IllegalArgumentException("Cannot request (%s) elements from TempPool: the pool size is (%s)"
                    .formatted(count, pool.length));
        }
        if (available > 0) {
            throw new IllegalStateException("Cannot request (%s) elements from TempPool: the pool still has (%s) available elements"
                    .formatted(count, available));
        }

        available = count;
    }

    /**
     * Retrieves the next available temporary element from the pool.
     * <p>
     * Must be preceded by a call to {@link #request(int)} to ensure safety and
     * prevent overwriting elements that might still be in use.
     *
     * @return the next pre-allocated instance of type {@code T}
     * @throws IllegalStateException if called without an active request or if
     *                               the requested quota has been exhausted
     */
    public T next() {
        if (available <= 0) {
            throw new IllegalStateException("Cannot retrieve next element from TempPool: no available elements");
        }

        available--;
        return pool[(current++) % pool.length];
    }
}