package pro.komaru.tridot.util.struct;

import pro.komaru.tridot.util.struct.func.Cons;
import pro.komaru.tridot.util.struct.func.Func;
import pro.komaru.tridot.util.struct.func.Prov;

/**
 * Fixed-size scratchpad pool for reusing temporary objects (vectors, matrices, etc.)
 * inside frame calculations or other hot paths, without allocating new instances.
 *
 * <p>Objects are handed out in a circular order. Usage follows a request contract:
 * call {@link #request(int)} with the number of objects needed, then call
 * {@link #next()} exactly that many times. Calling {@code next()} without an active
 * request, or requesting again before the previous request is fully consumed, throws
 * an exception.
 *
 * <p>If a resetter is supplied, it is applied to each element right before it is
 * handed out by {@link #next()}, so callers always receive a clean instance.
 *
 * <p><b>Lifetime:</b> the pool does not track which objects are still in use. Once the
 * ring wraps around, previously returned instances are handed out again. Callers must
 * not keep references to pooled objects beyond the block where they were obtained,
 * and must not allow more than {@code capacity} other {@code take()}/{@code next()}
 * calls to happen while a pooled object is still needed.
 *
 * @param <T> the type of elements managed by this pool
 */
public class TempPool<T> {
    private final T[] pool;
    private final Cons<T> resetter;
    private int current = 0;
    private int available = 0;

    /**
     * Creates a pool with a fixed number of pre-allocated elements and no resetter.
     * Elements keep whatever state they had when they are handed out again.
     *
     * @param capacity    the number of elements in the ring; must be greater than zero
     * @param initializer creates each element once, during construction
     */
    public TempPool(int capacity, Prov<T> initializer) {
        this(capacity, initializer, null);
    }

    /**
     * Creates a pool with a fixed number of pre-allocated elements.
     *
     * @param capacity    the number of elements in the ring; must be greater than zero
     * @param initializer creates each element once, during construction
     * @param resetter    applied to an element right before {@link #next()} returns it;
     *                    may be {@code null} to skip resetting
     */
    @SuppressWarnings("unchecked")
    public TempPool(int capacity, Prov<T> initializer, Cons<T> resetter) {
        pool = (T[]) new Object[capacity];
        this.resetter = resetter;

        for (int i = 0; i < capacity; i++) {
            pool[i] = initializer.get();
        }
    }

    /**
     * Borrows one element for the duration of {@code action} and returns the result
     * of the action. The element is not returned to the pool explicitly; it becomes
     * reusable once the ring wraps around.
     *
     * @param action the function applied to the borrowed element
     * @param <R>    the result type
     * @return the value produced by {@code action}
     */
    public <R> R use(Func<T, R> action) {
        T item = take();
        return action.get(item);
    }

    /**
     * Requests one element and returns it. Equivalent to {@code request(1).next()}.
     *
     * @return a pooled element; valid until the ring wraps around
     * @throws IllegalStateException if a previous request has not been fully consumed
     */
    public T take() {
        request(1);
        return next();
    }

    /**
     * Declares how many elements the next block of code will obtain via {@link #next()}.
     * Must be called before every group of {@code next()} calls.
     *
     * <p>This does not reserve or move the ring cursor; the cursor advances in
     * {@link #next()}. It only validates the request and sets the number of
     * elements that may be taken.
     *
     * @param count the number of elements that will be taken; must be {@code <= capacity}
     * @return this pool, for chaining
     * @throws IllegalArgumentException if {@code count} exceeds the pool capacity
     * @throws IllegalStateException    if the previous request still has unconsumed
     *                                  elements; call {@link #reset()} to clear it
     */
    public TempPool<T> request(int count) {
        if (count > pool.length) {
            throw new IllegalArgumentException("Cannot request (%s) elements from TempPool: the pool size is (%s)"
                    .formatted(count, pool.length));
        }
        if (available > 0) {
            throw new IllegalStateException("Cannot request (%s) elements from TempPool: the pool still has (%s) available elements"
                    .formatted(count, available));
        }

        available = count;
        return this;
    }

    /**
     * Discards any unconsumed quota from the current request. Use this to recover
     * after an exception was thrown between {@link #request(int)} and the matching
     * {@link #next()} calls. Does not change the ring cursor.
     */
    public void reset() {
        available = 0;
    }

    /**
     * Returns the next element in the ring and consumes one unit of the current request.
     *
     * @param resetState if {@code true} and a resetter was supplied, it is applied to the element first
     * @return a pooled element
     * @throws IllegalStateException if there is no active request, or the request
     *                               quota has already been consumed
     */
    public T next(boolean resetState) {
        if (available <= 0) {
            throw new IllegalStateException("Cannot retrieve next element from TempPool: no available elements");
        }

        available--;
        T item = pool[current];
        current++;
        if (current == pool.length) current = 0;

        if (resetState && resetter != null) {
            resetter.get(item);
        }
        return item;
    }

    /**
     * Same as {@link #next(boolean)} with {@code resetState = false}.
     * The returned element keeps whatever state it had before.
     *
     * @return a pooled element
     * @throws IllegalStateException if there is no active request, or the request
     *                               quota has already been consumed
     */
    public T next() {
        return next(false);
    }
}