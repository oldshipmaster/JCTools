package org.jctools.queues;

import org.jctools.queues.spec.ConcurrentQueueSpec;
import org.jctools.queues.spec.Ordering;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class MpmcArrayQueueSanityTest extends QueueSanityTest {
    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        // Mpmc minimal size is 2
        list.add(makeQueue(0, 0, 2, Ordering.FIFO, null));
        list.add(makeQueue(0, 0, SIZE, Ordering.FIFO, null));
        return list;
    }

    public MpmcArrayQueueSanityTest(ConcurrentQueueSpec spec, Queue<Integer> queue) {
        super(spec, queue);
    }

    @Test
    public void testOfferPollSemantics() throws Exception {
        final AtomicBoolean stop = new AtomicBoolean();
        final Queue<Integer> q = queue;
        // fill up the queue
        while (q.offer(1));
        // queue has 2 empty slots
        q.poll();
        q.poll();

        final Val fail = new Val();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop.get()) {
                    if(!q.offer(1))
                        fail.value++;
                    if(q.poll() == null)
                        fail.value++;
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop.get()) {
                    if(!q.offer(1))
                        fail.value++;
                    if(q.poll() == null)
                        fail.value++;
                }
            }
        });

        t1.start();
        t2.start();
        Thread.sleep(1000);
        stop.set(true);
        t1.join();
        t2.join();
        assertEquals("Unexpected offer/poll observed", 0, fail.value);

    }

}
