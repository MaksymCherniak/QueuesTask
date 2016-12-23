import org.junit.Before;
import org.junit.Test;
import test.task.queues.MostRecentlyInsertedQueue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TestMostRecentlyInsertedQueue {
    private MostRecentlyInsertedQueue<Integer> queue;

    @Before
    public void init() {
        queue = new MostRecentlyInsertedQueue<Integer>(3);
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        queue.offer(4);
        queue.offer(5);
    }

    @Test
    public void offerIsImpossible() {
        queue.clear();

        assertThat(queue.offer(6), is(Boolean.TRUE));

        assertThat(queue.size(), is(1));
    }

    @Test
    public void sizeMethodTest() {
        assertThat(queue.size(), is(3));
    }

    @Test
    public void pollOperationTest() {
        assertThat(queue.poll(), is(3));
    }

    @Test
    public void secondPollOperationTest() {
        queue.clear();

        queue.offer(1);

        assertThat(queue.poll(), is(1));
    }

    @Test
    public void sizeAfterPollTest() {
        queue.poll();

        assertThat(queue.size(), is(2));
    }

    @Test
    public void peekOperationTest() {
        assertThat(queue.peek(), is(3));

        assertThat(queue.size(), is(3));
    }

    @Test
    public void removeOperationTest() {
        queue.remove(3);

        assertThat(queue.size(), is(2));

        assertThat(queue.peek(), is(4));
    }

    @Test
    public void isEmptyOperationTest() {
        assertThat(queue.isEmpty(), is(false));
    }

    @Test
    public void secondSsEmptyOperationTest() {
        queue.clear();

        assertThat(queue.isEmpty(), is(true));
    }

    @Test
    public void canNotRemoveIfNotExist() {
        assertThat(queue.remove(10), is(false));
    }

    @Test(expected = NullPointerException.class)
    public void throwedExceptionIfCommingNullObject() {
        queue.offer(null);
    }
}
