package elevatorsim;

import java.util.ArrayList;

// One elevator car. Each elevator runs on its own thread (see run()).
// ref: https://www.geeksforgeeks.org/java/lifecycle-and-states-of-a-thread-in-java/
// idea: the elevator thread goes through these Thread states:
//   RUNNABLE       moving one floor, printing (actually running code)
//   TIMED_WAITING  Thread.sleep() between floors and while the doors are open
//   WAITING        wait() when idle with no stops, until addStop() or stopRunning() calls notifyAll
// ref: https://www.geeksforgeeks.org/java/arraylist-in-java/
// idea: stops are kept in an ArrayList in the order they were added, the first one is served first
// ref: https://www.geeksforgeeks.org/java/runnable-interface-in-java/
// idea: run() is the code a thread executes, so each elevator gets its own run() loop
public class Elevator implements Runnable {

    private int id;
    private int currentFloor = Config.BOTTOM_FLOOR;  // every elevator starts in the lobby
    private ElevatorState state = ElevatorState.IDLE;
    private Direction direction = Direction.NONE;    // NONE while standing still
    private ArrayList<Integer> stops = new ArrayList<Integer>();

    // ref: https://www.geeksforgeeks.org/java/volatile-keyword-in-java/
    // idea: volatile so the elevator thread sees the change when the main thread sets it to false
    private volatile boolean running = true;

    public Elevator(int id) {
        this.id = id;
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

    public synchronized boolean hasStops() {
        return !stops.isEmpty();
    }

    // Moves one floor toward the first stop. Returns true if it arrived at a stop (doors should open).
    public synchronized boolean moveOneStep() {    // synchronized: changes stops, which addStop() also changes
        if (stops.isEmpty()) {
            return false;   // nothing to do
        }
        int target = stops.get(0);
        if (target > currentFloor) {
            currentFloor++;
            direction = Direction.UP;
            state = ElevatorState.MOVING_UP;
        } else if (target < currentFloor) {
            currentFloor--;
            direction = Direction.DOWN;
            state = ElevatorState.MOVING_DOWN;
        }
        if (currentFloor == target) {
            arriveAtFloor();
            return true;
        }
        return false;
    }

    // Reached the first stop: drop it and open the doors. Called with the lock held (from moveOneStep).
    private void arriveAtFloor() {
        stops.remove(0);
        state = ElevatorState.DOORS_OPEN;
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
