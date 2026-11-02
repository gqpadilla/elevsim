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
        RequestQueue queue = new RequestQueue();
        Building building = new Building(queue);    // elevators get the queue so a full one can give requests back
        Thread dispatcher = new Thread(new Dispatcher(building, queue), "Dispatcher");

        building.startAll();
        dispatcher.start();

        // 8 passengers, going up and down from different floors (arbitrary, spread over the lower 30 floors)
        int[][] trips = { {1, 12}, {5, 20}, {15, 3}, {8, 2}, {20, 30}, {30, 10}, {2, 9}, {12, 25} };
        for (int i = 0; i < trips.length; i++) {
            Request r = new Request(0, trips[i][0], trips[i][1]);
            System.out.println("main: new request " + r);
            queue.add(r);
            Thread.sleep(700);  // arbitrary gap between requests, so the elevators are already moving
        }

        Thread.sleep(35000);    // arbitrary: the longest ride here is ~56 floors (~28 s) plus door stops
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
