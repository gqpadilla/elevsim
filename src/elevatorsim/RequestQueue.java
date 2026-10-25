package elevatorsim;

import java.util.ArrayList;

// Shared queue of passenger requests. Input threads add(), the dispatcher take()s.
// ref: https://www.geeksforgeeks.org/java/producer-consumer-solution-using-threads-java/
// ref: https://www.geeksforgeeks.org/java/inter-thread-communication-java/
// idea: take() waits while the queue is empty, add() calls notifyAll so the waiting thread wakes up
public class RequestQueue {

    private ArrayList<Request> list = new ArrayList<Request>();
    private boolean open = true;    // false after close(), no more requests will come

    // synchronized so two threads can't change the list at the same time
    public synchronized void add(Request r) {
        if (!open) {
            return;     // queue is closed, nobody will take this request anymore
        }
        list.add(r);
        notifyAll();    // wake up any thread waiting in take()
    }

    // Returns the oldest request, waiting if the queue is empty. Returns null once closed and empty.
    public synchronized Request take() throws InterruptedException {
        while (list.isEmpty() && open) {
            wait();     // while, not if: re-check after waking up (spurious wakeups, or another thread took it first)
        }
        if (list.isEmpty()) {
            return null;    // woke up because the queue was closed, and nothing is left
        }
        return list.remove(0);  // first in, first out
    }

    // No more requests. Wakes up waiting threads so they can see the queue is closed and finish.
    public synchronized void close() {
        open = false;
        notifyAll();    // a thread stuck in take() would otherwise wait forever
    }

    public synchronized int size() {
        return list.size();
    }
}
