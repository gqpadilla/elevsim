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
                SimLogger.log(r + " -> Elevator " + e.getId());  // print first, so it shows before the elevator moves
                e.assignRequest(r);
                r = queue.take();
            }
        } catch (InterruptedException e) {
            // interrupted while waiting in take(), just stop
        }
        SimLogger.log("stopped");
    }

    // Picks the elevator with the lowest cost (about how many floors it travels before reaching the pickup):
    //   idle                                         -> cost = distance
    //   moving toward the pickup, same way as request -> cost = distance (it passes the floor on the way)
    //   otherwise -> it first finishes its trip: distance to its farthest stop + from there to the pickup
    //   full (load >= CAPACITY) -> +100 on top, so a full elevator is only picked if every elevator is full
    private Elevator chooseElevator(Request r) {
        Elevator best = null;
        int bestCost = Integer.MAX_VALUE;   // so the first elevator always counts as cheaper
        for (Elevator e : building.getElevators()) {
            int cost = cost(e, r);
            if (cost < bestCost) {
                best = e;
                bestCost = cost;
            }
        }
        return best;
    }

    // shortcut: reads floor/state/direction with separate calls, so the elevator may move in between;
    // fine for an estimate, read them in one synchronized call if exact numbers ever matter
    private int cost(Elevator e, Request r) {
        int cost = travelCost(e, r);
        if (e.getLoad() >= Config.CAPACITY) {
            cost += 100;    // arbitrary big penalty (more than any trip in a 50 floor building) so a full elevator is picked last
        }
        return cost;
    }

    // How many floors the elevator travels before it can reach the pickup floor.
    private int travelCost(Elevator e, Request r) {
        int floor = e.getCurrentFloor();
        int pickup = r.getFromFloor();
        int distance = Math.abs(floor - pickup);    // floors away, sign doesn't matter
        if (e.getState() == ElevatorState.IDLE) {
            return distance;
        }
        Direction d = e.getDirection();
        boolean onTheWayUp = d == Direction.UP && pickup >= floor && r.getDirection() == Direction.UP;
        boolean onTheWayDown = d == Direction.DOWN && pickup <= floor && r.getDirection() == Direction.DOWN;
        if (onTheWayUp || onTheWayDown) {
            return distance;
        }
        int farthest = e.getFarthestStop();
        return Math.abs(farthest - floor) + Math.abs(farthest - pickup);
    }
}
