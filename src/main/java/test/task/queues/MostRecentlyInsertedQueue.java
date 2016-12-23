package test.task.queues;

import java.util.*;

public class MostRecentlyInsertedQueue<E> extends AbstractQueue<E> implements Queue<E> {
    private Object[] elements;
    private int size = 0;
    private int modCount = 0;

    public MostRecentlyInsertedQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }

        elements = new Object[capacity];
    }

    @Override
    public Iterator iterator() {
        return new Itr();
    }

    @Override
    public int size() {
        return size;
    }

    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        modCount++;
        int i = size;
        if (i >= elements.length) {
            System.arraycopy(elements, 1, elements, 0, i - 1);
            size--;
            return offer(e);
        } else {
            size = i + 1;
            elements[i] = e;
        }

        return true;
    }

    public E poll() {
        if (size == 0) {
            return null;
        }

        int s = --size;
        modCount++;
        E result = (E) elements[0];
        System.arraycopy(elements, 1, elements, 0, s);
        elements[s] = null;

        return result;
    }

    public E peek() {
        return (size == 0) ? null : (E) elements[0];
    }

    private final class Itr implements Iterator<E> {
        private int cursor = 0;
        private int lastRet = -1;
        private ArrayDeque<E> forgetMeNot = null;
        private E lastRetElt = null;
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size ||
                    (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (cursor < size)
                return (E) elements[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (lastRet != -1) {
                E moved = MostRecentlyInsertedQueue.this.poll();
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<E>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                MostRecentlyInsertedQueue.this.poll();
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }
    }

}
