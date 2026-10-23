package elevatorsim;

// The building holds all the elevators. Single-threaded for now: Main moves them one after another.
public class Building {

    private Elevator[] elevators = new Elevator[Config.NUM_ELEVATORS];
    private Thread[] threads = new Thread[Config.NUM_ELEVATORS];  // kept so Day 7 can join() them

    public Building() {
        // ids 1..4 for the elevators, matching the numbers people see on the screen
        for (int i = 0; i < elevators.length; i++) {
            elevators[i] = new Elevator(i + 1);  // array index starts at 0, elevator ids start at 1
        }
    }

    // id is 1-based, so elevator 1 is at index 0
    public Elevator getElevator(int id) {
        return elevators[id - 1];
    }

    public Elevator[] getElevators() {
        return elevators;
    }

    // One line per elevator.
    public void printStatus() {
        for (Elevator e : elevators) {
            System.out.println("  " + e);
        }
    }

    // Starts one thread per elevator. The thread name shows up in the output, e.g. "Elevator-1".
    // ref: https://www.geeksforgeeks.org/java/java-threads/
    // idea: start() creates a new thread that runs the elevator's run() method at the same time as the others
    public void startAll() {
        for (int i = 0; i < elevators.length; i++) {
            threads[i] = new Thread(elevators[i], "Elevator-" + elevators[i].getId());
            threads[i].start();
        }
    }
}
