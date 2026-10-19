package elevatorsim;

// One passenger call: someone at fromFloor wants to go to toFloor.
public class Request {

    // TODO: not thread safe yet, fix when many threads create requests (fixed on Day 20)
    private static int nextId = 1;  // ids start at 1 so the first passenger is P1

    private int id;
    private int time;       // seconds after start, from the CSV (0 for requests typed in by hand)
    private int fromFloor;  // pickup floor
    private int toFloor;    // destination floor

    public Request(int time, int fromFloor, int toFloor) {
        this.id = nextId++;     // give every new request its own number
        this.time = time;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    // UP if the passenger wants a higher floor, otherwise DOWN.
    // (from == to is rejected by isValid, so NONE never comes out of here)
    public Direction getDirection() {
        if (toFloor > fromFloor) {
            return Direction.UP;
        }
        return Direction.DOWN;
    }

    public int getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    // A floor is valid if it is between the lobby and the top floor (both included).
    public static boolean isValidFloor(int f) {
        return f >= Config.BOTTOM_FLOOR && f <= Config.NUM_FLOORS;
    }

    // A trip is valid if both floors exist and the passenger actually moves.
    public static boolean isValid(int from, int to) {
        return isValidFloor(from) && isValidFloor(to) && from != to;
    }

    // Short form used in the console, e.g. "P3 (5 -> 32)".
    @Override
    public String toString() {
        return "P" + id + " (" + fromFloor + " -> " + toFloor + ")";
    }
}
