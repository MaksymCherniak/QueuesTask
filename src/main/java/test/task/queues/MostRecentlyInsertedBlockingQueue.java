package test.task.queues;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MostRecentlyInsertedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    private Object[] elements;
    private int size = 0;
    private final ReentrantLock lock;
    private final Condition notEmpty;

    public MostRecentlyInsertedBlockingQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }

        elements = new Object[capacity];
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    @Override
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) throws InterruptedException {
        offer(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return offer(e);
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ((result = dequeue()) == null)
                notEmpty.await();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ((result = dequeue()) == null && nanos > 0)
                nanos = notEmpty.awaitNanos(nanos);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        final ReentrantLock lock = this.lock;
        lock.lock();

        int i = size;
        try {
            if (i >= elements.length) {
                System.arraycopy(elements, 1, elements, 0, i - 1);
                size--;
                return offer(e);
            } else {
                size = i + 1;
                elements[i] = e;
            }
            notEmpty.signal();
        } finally {
            lock.unlock();
        }

        return true;
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (size == 0) ? null : (E) elements[0];
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i == -1) {
                return false;
            }
            removeAt(i);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return indexOf(o) != -1;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return Arrays.copyOf(elements, size);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = elements;
            int n = size;
            size = 0;
            for (int i = 0; i < n; i++)
                array[i] = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (a.length < n)
                return (T[]) Arrays.copyOf(elements, size, a.getClass());
            System.arraycopy(elements, 0, a, 0, n);
            if (a.length > n)
                a[n] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    private E dequeue() {
        int n = size - 1;
        if (n < 0)
            return null;
        else {
            E result = (E) elements[0];
            System.arraycopy(elements, 1, elements, 0, n);
            elements[n] = null;
            size = n;
            return result;
        }
    }

    private int indexOf(Object o) {
        if (o != null) {
            Object[] array = elements;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.equals(array[i]))
                    return i;
        }
        return -1;
    }

    private void removeAt(int i) {
        Object[] array = elements;
        int n = size - 1;
        if (n == i)
            array[i] = null;
        else {
            System.arraycopy(elements, i + 1, elements, i, n);
            array[n] = null;
        }
        size = n;
    }

    void removeEQ(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = elements;
            for (int i = 0, n = size; i < n; i++) {
                if (o == array[i]) {
                    removeAt(i);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    final class Itr implements Iterator<E> {
        final Object[] array;
        int cursor;
        int lastRet;

        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E) array[cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            removeEQ(array[lastRet]);
            lastRet = -1;
        }
    }

    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
}
