package elevatorsim;

// Prints simulation messages with the time since start and the name of the thread that printed them.
public class SimLogger {

    private static long startTime = System.currentTimeMillis();   // until start() is called, time counts from class load

    // Call once when the simulation starts, so times begin at 0.0 s.
    public static synchronized void start() {
        startTime = System.currentTimeMillis();
    }

    // ref: https://www.geeksforgeeks.org/java/synchronization-in-java/
    // idea: static synchronized so only one thread prints a line at a time
    public static synchronized void log(String msg) {
        double seconds = (System.currentTimeMillis() - startTime) / 1000.0;   // ms -> s
        // %6.1f: 6 characters wide, 1 decimal, so the columns line up ("[  12.4s]")
        System.out.println(String.format("[%6.1fs] [%s] %s", seconds, Thread.currentThread().getName(), msg));
    }

    // private so nobody can do "new SimLogger()", this class only has static methods
    private SimLogger() {
    }
}
