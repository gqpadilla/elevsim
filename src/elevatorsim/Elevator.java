package elevatorsim;

import java.util.ArrayList;

// One elevator car. For now it moves only when moveOneStep() is called by hand (no threads yet).
// ref: https://www.geeksforgeeks.org/java/arraylist-in-java/
// idea: stops are kept in an ArrayList in the order they were added, the first one is served first
public class Elevator {

    private int id;
    private int currentFloor = Config.BOTTOM_FLOOR;  // every elevator starts in the lobby
    private ElevatorState state = ElevatorState.IDLE;
    private Direction direction = Direction.NONE;    // NONE while standing still
    private ArrayList<Integer> stops = new ArrayList<Integer>();

    public Elevator(int id) {
        this.id = id;
    }

    // Adds a floor to the stop list. Invalid floors and floors already in the list are ignored.
    public void addStop(int floor) {
        if (!Request.isValidFloor(floor) || stops.contains(floor)) {
            return;
        }
        stops.add(floor);
    }

    public boolean hasStops() {
        return !stops.isEmpty();
    }

    // Moves one floor toward the first stop. Does nothing if there are no stops.
    public void moveOneStep() {
        if (stops.isEmpty()) {
            state = ElevatorState.IDLE;
            direction = Direction.NONE;
            return;
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

        // Arrived: drop the stop. If it was the last one, the elevator is idle again.
        if (currentFloor == target) {
            stops.remove(0);
            if (stops.isEmpty()) {
                state = ElevatorState.IDLE;
                direction = Direction.NONE;
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public ElevatorState getState() {
        return state;
    }

    public Direction getDirection() {
        return direction;
    }

    public ArrayList<Integer> getStops() {
        return stops;
    }

    // Short form used in the console, e.g. "Elevator 1 | floor 7 | MOVING_UP | stops [10, 3]".
    @Override
    public String toString() {
        return "Elevator " + id + " | floor " + currentFloor + " | " + state + " | stops " + stops;
    }
}
