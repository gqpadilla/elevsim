package elevatorsim;

// ref: https://www.geeksforgeeks.org/java/enum-in-java/
// idea: an enum gives a fixed list of named states, so a typo like "MOVNG" won't compile
public enum ElevatorState {
    IDLE,           // standing still, no stops to go to
    MOVING_UP,
    MOVING_DOWN,
    DOORS_OPEN,     // stopped at a floor, people getting in/out
    OUT_OF_SERVICE  // TODO: used by maintenance mode later (stop/fix commands)
}
