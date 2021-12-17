package spacegraph.audio.synth.string;

import java.util.Iterator;

/** TODO try IntArrayFIFOQueue */
@Deprecated public class ArrayRingBuffer<T> extends AbstractBoundedQueue<T> {
    private int first;
    private int last;
    private final T[] rb;

    public ArrayRingBuffer(int capacity) {
        rb = (T[]) new Object[capacity];
        first = 0;
        last = 0;
        fillCount = 0;
        this.capacity = capacity;
    }

    /**
     * Adds x to the end of the ring buffer. If there is no room, then
     * throw new RuntimeException("Ring buffer overflow"). Exceptions
     * covered Monday.
     */
    public void enqueue(T x) {
        if (isFull()) {
            throw new RuntimeException("Ring buffer overflow");
        } else {
            rb[last] = x;
            last = increment(last);
            fillCount++;
        }
    }

    /**
     * Dequeue oldest item in the ring buffer. If the buffer is empty, then
     * throw new RuntimeException("Ring buffer underflow"). Exceptions
     * covered Monday.
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("Ring buffer underflow");
        } else {
            T toReturn = peek();
            first = increment(first);
            fillCount--;
            return toReturn;
        }
    }

    /**
     * Return oldest item, but don't remove it.
     */
    public T peek() {
//        if (isEmpty()) {
//            throw new RuntimeException("Ring buffer underflow");
//        } else {
            return rb[first];
//        }
    }
    public void set(int i, T t) {
        rb[mod(first + i)] = t;
    }


    public T get(int index) {
//        if (isEmpty()) {
//            throw new RuntimeException("Ring buffer underflow");
//        } else {
        return rb[mod(first + index)];
//        }
    }

    private int mod(int x) {
        return x % capacity;
    }

    public Iterator<T> iterator() {
        return new ArrayRingBufferIterator();
    }

    private int increment(int x) {
        return (x + 1) % capacity;
    }

    private class ArrayRingBufferIterator implements Iterator<T> {

        int index;

        ArrayRingBufferIterator() {
            index = 0;
        }

        public T next() {
            T toReturn = get(index);
            index++;
            return toReturn;
        }

        public boolean hasNext() {
            return (index < fillCount);
        }
    }
}
