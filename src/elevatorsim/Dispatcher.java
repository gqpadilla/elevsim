package elevatorsim;

// Takes requests from the queue and gives each one to an elevator. Runs on its own thread.
// ref: https://www.geeksforgeeks.org/java/producer-consumer-solution-using-threads-java/
// idea: the dispatcher is the consumer: it waits in queue.take() until main (the producer) adds a request
public class Dispatcher implements Runnable {

    private Building building;
    private RequestQueue queue;

    public Dispatcher(Building building, RequestQueue queue) {
        this.building = building;
        this.queue = queue;
    }

    // Ends when the queue is closed and empty (take() returns null), so no running flag is needed.
    @Override
    public void run() {
        try {
            Request r = queue.take();
            while (r != null) {     // null means the queue is closed and empty
                // take() has returned, so the queue lock is released here:
                // don't hold queue lock while calling elevator (avoid deadlock)
                Elevator e = chooseElevator(r);
                e.addStop(r.getFromFloor());
                // TODO: only goes to the pickup floor for now, passengers ride to toFloor on Day 13
                System.out.println("Dispatcher: " + r + " -> Elevator " + e.getId());
                r = queue.take();
            }
        } catch (InterruptedException e) {
            // interrupted while waiting in take(), just stop
        }
        System.out.println("Dispatcher stopped");
    }

    // Simple version: the elevator closest to the pickup floor. On a tie, an IDLE one wins.
    private Elevator chooseElevator(Request r) {
        Elevator best = null;
        int bestDistance = Integer.MAX_VALUE;   // so the first elevator always counts as closer
        for (Elevator e : building.getElevators()) {
            int distance = Math.abs(e.getCurrentFloor() - r.getFromFloor());  // floors away, sign doesn't matter
            boolean tieButIdle = distance == bestDistance
                    && e.getState() == ElevatorState.IDLE
                    && best.getState() != ElevatorState.IDLE;
            if (distance < bestDistance || tieButIdle) {
                best = e;
                bestDistance = distance;
            }
        }
        return best;
    }
}
