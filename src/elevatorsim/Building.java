package elevatorsim;

// The building holds all the elevators. Single-threaded for now: Main moves them one after another.
public class Building {

    private Elevator[] elevators = new Elevator[Config.NUM_ELEVATORS];

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
}
