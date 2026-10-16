package elevatorsim;

// All the fixed numbers of the simulation in one place, so they're easy to find and change.
public class Config {

    public static final int NUM_ELEVATORS = 4;      // project requirement
    public static final int NUM_FLOORS = 50;        // project requirement
    public static final int BOTTOM_FLOOR = 1;       // lobby is floor 1 (no basement in this sim)
    public static final int FLOOR_TRAVEL_MS = 500;  // arbitrary, fast enough to watch, slow enough to follow
    public static final int DOOR_OPEN_MS = 1000;    // arbitrary, about how long real doors stay open, shortened
    public static final int CAPACITY = 10;          // typical small office elevator holds ~10 people

    // private so nobody can do "new Config()", this class only holds constants
    private Config() {
    }
}
