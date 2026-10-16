package elevatorsim;

// Which way an elevator is heading (or which way a passenger wants to go).
public enum Direction {
    UP,
    DOWN,
    NONE    // not heading anywhere, e.g. an idle elevator with no stops
}
