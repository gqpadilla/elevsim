package elevatorsim;

import java.util.ArrayList;

// One elevator car. Each elevator runs on its own thread (see run()).
// ref: https://www.geeksforgeeks.org/java/lifecycle-and-states-of-a-thread-in-java/
// idea: the elevator thread goes through these Thread states:
//   RUNNABLE       moving one floor, printing (actually running code)
//   TIMED_WAITING  Thread.sleep() between floors and while the doors are open
//   WAITING        wait() when idle with no stops, until addStop() or stopRunning() calls notifyAll
// ref: https://www.geeksforgeeks.org/java/arraylist-in-java/
// idea: stops are kept in an ArrayList; the order doesn't matter, SCAN picks the next one (see moveOneStep)
// ref: https://www.geeksforgeeks.org/java/runnable-interface-in-java/
// idea: run() is the code a thread executes, so each elevator gets its own run() loop
public class Elevator implements Runnable {

    private int id;
    private int currentFloor = Config.BOTTOM_FLOOR;  // every elevator starts in the lobby
    private ElevatorState state = ElevatorState.IDLE;
    private Direction direction = Direction.NONE;    // NONE while standing still
    private ArrayList<Integer> stops = new ArrayList<Integer>();
    private ArrayList<Request> pickups = new ArrayList<Request>();      // assigned to us, still waiting on their floor
    private ArrayList<Request> passengers = new ArrayList<Request>();   // inside the elevator
    private RequestQueue queue;     // where requests go back if we are full (set by Building)

    // ref: https://www.geeksforgeeks.org/java/volatile-keyword-in-java/
    // idea: volatile so the elevator thread sees the change when the main thread sets it to false
    private volatile boolean running = true;

    public Elevator(int id) {
        this.id = id;
    }

    // Called by Building, so a full elevator can hand requests back to the dispatcher.
    public void setQueue(RequestQueue queue) {
        this.queue = queue;
    }

    // ref: https://www.geeksforgeeks.org/java/synchronization-in-java/
    // idea: other threads (main now, the dispatcher later) call these while the elevator thread runs,
    //       so every method that touches stops/floor/state is synchronized on this elevator

    // Adds a floor to the stop list. Invalid floors and floors already in the list are ignored.
    public synchronized void addStop(int floor) {
        if (!Request.isValidFloor(floor) || stops.contains(floor)) {
            return;
        }
        stops.add(floor);
        notifyAll();    // wake the elevator thread if it is waiting with no stops
    }

    // The dispatcher gives us a passenger: remember them and go to their floor.
    public synchronized void assignRequest(Request r) {
        pickups.add(r);
        addStop(r.getFromFloor());  // same lock (synchronized methods can call each other), so this is fine
    }

    // People inside plus people we still have to pick up. The dispatcher uses it to avoid full elevators.
    public synchronized int getLoad() {
        return passengers.size() + pickups.size();
    }

    public synchronized boolean hasStops() {
        return !stops.isEmpty();
    }

    // ref: https://www.geeksforgeeks.org/operating-systems/scan-elevator-disk-scheduling-algorithms/
    // idea: SCAN = keep going the same way while there are stops ahead, only turn around when there are none

    // Moves one floor using SCAN. Returns true if it arrived at a stop (doors should open).
    public synchronized boolean moveOneStep() {    // synchronized: changes stops, which addStop() also changes
        if (stops.isEmpty()) {
            return false;   // nothing to do
        }
        if (stops.contains(currentFloor)) {
            arriveAtFloor();    // a stop for the floor we're already on (e.g. added while the doors were open)
            return true;
        }
        chooseDirection();
        if (direction == Direction.UP) {
            currentFloor++;
            state = ElevatorState.MOVING_UP;
        } else {
            currentFloor--;
            state = ElevatorState.MOVING_DOWN;
        }
        if (stops.contains(currentFloor)) {
            arriveAtFloor();    // any stop counts, not just the first one added: that's what stops the zig-zag
            return true;
        }
        return false;
    }

    // Keep the current direction while there are stops ahead, otherwise turn around. Lock held by caller.
    private void chooseDirection() {
        if (direction == Direction.UP && hasStopAbove()) {
            return;     // still stops above, keep going up
        }
        if (direction == Direction.DOWN && hasStopBelow()) {
            return;     // still stops below, keep going down
        }
        // nothing ahead (or we were idle): go wherever the stops are. stops isn't empty and none is on this floor,
        // so one of the two is true
        if (hasStopAbove()) {
            direction = Direction.UP;
        } else {
            direction = Direction.DOWN;
        }
    }

    private boolean hasStopAbove() {
        for (int f : stops) {
            if (f > currentFloor) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStopBelow() {
        for (int f : stops) {
            if (f < currentFloor) {
                return true;
            }
        }
        return false;
    }

    // The last stop in the current direction (where the elevator will turn around). Current floor if none.
    public synchronized int getFarthestStop() {
        int farthest = currentFloor;
        for (int f : stops) {
            if (direction == Direction.UP && f > farthest) {
                farthest = f;
            } else if (direction == Direction.DOWN && f < farthest) {
                farthest = f;
            }
        }
        return farthest;
    }

    // Reached a stop: drop it, open the doors, let people out and in. Called with the lock held (from moveOneStep).
    private void arriveAtFloor() {
        stops.remove(Integer.valueOf(currentFloor));    // Integer.valueOf: remove the floor value, not the item at that index
        state = ElevatorState.DOORS_OPEN;
        dropOffPassengers();
        pickUpPassengers();
    }

    // Everyone whose destination is this floor gets out.
    private void dropOffPassengers() {
        // backwards, so removing an item doesn't shift the ones we haven't checked yet
        for (int i = passengers.size() - 1; i >= 0; i--) {
            Request p = passengers.get(i);
            if (p.getToFloor() == currentFloor) {
                passengers.remove(i);
                System.out.println(p + " dropped at " + currentFloor + " by Elevator " + id);
            }
        }
    }

    // Everyone waiting on this floor gets in while there's room. The rest go back to the queue.
    private void pickUpPassengers() {
        for (int i = pickups.size() - 1; i >= 0; i--) {     // backwards, same reason as dropOffPassengers()
            Request p = pickups.get(i);
            if (p.getFromFloor() != currentFloor) {
                continue;   // waiting on another floor
            }
            pickups.remove(i);
            if (passengers.size() < Config.CAPACITY) {
                passengers.add(p);
                addStop(p.getToFloor());
                System.out.println(p + " picked up at " + currentFloor + " by Elevator " + id);
            } else {
                // ref: https://www.geeksforgeeks.org/java/producer-consumer-solution-using-threads-java/
                // idea: the elevator becomes a producer here, the dispatcher takes the request again
                // safe to call while holding our lock: nobody holds the queue lock while waiting for an elevator
                System.out.println(p + " left at " + currentFloor + ", Elevator " + id + " is full");
                queue.add(p);
            }
        }
    }

    // Doors are closed again: idle if there's nothing left, otherwise the next moveOneStep() sets MOVING_*.
    public synchronized void closeDoors() {
        if (stops.isEmpty()) {
            state = ElevatorState.IDLE;
            direction = Direction.NONE;
        }
    }

    // Runs on the elevator's own thread until stopRunning() is called or the thread is interrupted.
    @Override
    public void run() {
        try {
            while (running) {   // checked every loop, so the thread ends soon after stopRunning()
                synchronized (this) {
                    while (running && stops.isEmpty()) {
                        wait();     // idle: sleep until addStop() or stopRunning() calls notifyAll (while: re-check after waking)
                    }
                }
                if (!running) {
                    break;      // woken up by stopRunning(), not by a new stop
                }
                // move and sleep outside the lock: while sleeping, other threads must still be able to add stops
                boolean arrived = moveOneStep();
                System.out.println(Thread.currentThread().getName() + " " + this);
                if (arrived) {
                    openAndCloseDoors();
                } else {
                    Thread.sleep(Config.FLOOR_TRAVEL_MS);   // one floor takes visible time
                }
            }
        } catch (InterruptedException e) {
            // interrupt() woke the thread up from sleep() or wait(), so leave the loop right away
        }
        System.out.println(Thread.currentThread().getName() + " stopped");
    }

    // Doors open, people get in/out, doors close. Runs on the elevator thread, sleeps without holding the lock.
    private void openAndCloseDoors() throws InterruptedException {
        String name = Thread.currentThread().getName();
        System.out.println(name + " doors open at floor " + getCurrentFloor());
        // ref: https://www.geeksforgeeks.org/java/thread-sleep-method-in-java-with-examples/
        // idea: sleep() pauses only this elevator's thread, the other elevators keep moving
        Thread.sleep(Config.DOOR_OPEN_MS);  // not inside synchronized: main must still be able to add stops meanwhile
        closeDoors();
        System.out.println(name + " doors closed");
    }

    // Called from another thread (main) to ask this elevator to finish its loop.
    public synchronized void stopRunning() {
        running = false;
        notifyAll();    // an idle elevator is stuck in wait(), wake it so it sees running == false
    }

    public int getId() {
        return id;
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized ElevatorState getState() {
        return state;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    // Short form used in the console, e.g. "Elevator 1 | floor 7 | MOVING_UP | stops [10, 3]".
    @Override
    public synchronized String toString() {
        return "Elevator " + id + " | floor " + currentFloor + " | " + state + " | stops " + stops;
    }
}
