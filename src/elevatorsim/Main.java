package elevatorsim;

// Entry point of the program: prints the banner and config, runs the small tests, then the simulation.
public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================");    // just a divider line for the banner
        System.out.println(" Elevator Simulation (Java Threads)");
        System.out.println(" " + Config.NUM_ELEVATORS + " elevators, " + Config.NUM_FLOORS + " floors");
        System.out.println("=================================");
        printConfig();
        testRequests();
        testElevator();
        runSimulation();
    }

    // Makes a few sample requests and checks the direction and validity of each.
    private static void testRequests() {
        System.out.println("Requests:");
        Request[] samples = {
            new Request(0, 5, 32),   // going up
            new Request(0, 40, 3),   // going down
            new Request(0, 1, 50),   // lobby to top floor, the longest trip
        };
        for (Request r : samples) {
            System.out.println("  " + r + " " + r.getDirection());
        }

        // Invalid pair: from == to, so no Request is created (that would use up an id).
        int from = 5;
        int to = 5;
        System.out.println("  " + from + " -> " + to + " valid? " + Request.isValid(from, to));
        System.out.println();   // blank line before the next message
    }

    // Moves one elevator through its stops, one floor per call, printing every step.
    private static void testElevator() {
        System.out.println("Elevator:");
        Elevator e = new Elevator(1);
        e.addStop(10);
        e.addStop(3);
        System.out.println("  " + e);

        // Loop ends when the last stop is reached, so the elevator is idle at floor 3.
        while (e.hasStops()) {
            if (e.moveOneStep()) {
                e.closeDoors();     // no thread here, so close the doors right away instead of waiting
            }
            System.out.println("  " + e);
        }
        System.out.println();   // blank line before the next message
    }

    // Main is the producer: it adds requests to the queue, the dispatcher thread hands them to the elevators.
    private static void runSimulation() throws InterruptedException {
        System.out.println("Simulation:");
        Building building = new Building();
        RequestQueue queue = new RequestQueue();
        Thread dispatcher = new Thread(new Dispatcher(building, queue), "Dispatcher");

        building.startAll();
        dispatcher.start();

        // E1 heads up to 20, then pickups at 8 and 14 come in below it on the same way up: SCAN serves 8, 14, 20, 30
        // in one sweep instead of zig-zagging. 30 -> 2 comes from the top end, 3 -> 1 from the bottom end.
        int[][] trips = { {20, 45}, {8, 30}, {14, 40}, {30, 2}, {3, 1} };
        int[] gaps = { 1000, 1000, 500, 500, 0 };   // arbitrary ms between requests, so E1 is already moving up
        for (int i = 0; i < trips.length; i++) {
            Request r = new Request(0, trips[i][0], trips[i][1]);
            System.out.println("main: new request " + r);
            queue.add(r);
            Thread.sleep(gaps[i]);
        }

        Thread.sleep(17000);    // arbitrary: E1's sweep 1 -> 30 is 29 floors (~14.5 s) plus 4 door stops (~4 s)
        queue.close();          // no more requests, the dispatcher's take() returns null
        dispatcher.join();      // wait until the dispatcher has finished
        building.stopAll();
        System.out.println("all threads finished");
    }

    // Prints every value from Config so the settings can be checked at a glance.
    private static void printConfig() {
        System.out.println("Config:");
        System.out.println("  elevators:     " + Config.NUM_ELEVATORS);
        System.out.println("  floors:        " + Config.BOTTOM_FLOOR + " to " + Config.NUM_FLOORS);
        System.out.println("  floor travel:  " + Config.FLOOR_TRAVEL_MS + " ms");
        System.out.println("  doors open:    " + Config.DOOR_OPEN_MS + " ms");
        System.out.println("  capacity:      " + Config.CAPACITY + " people");
        System.out.println();   // blank line before the next message
    }
}
