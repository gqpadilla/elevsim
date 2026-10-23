package elevatorsim;

// Entry point of the program. For now it only prints a banner and the config values.
public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================");    // just a divider line for the banner
        System.out.println(" Elevator Simulation (Java Threads)");
        System.out.println(" " + Config.NUM_ELEVATORS + " elevators, " + Config.NUM_FLOORS + " floors");
        System.out.println("=================================");
        printConfig();
        testRequests();
        testElevator();
        testBuilding();
        System.out.println("Simulation not implemented yet.");
        // TODO: start the simulation here once the classes exist
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
            e.moveOneStep();
            System.out.println("  " + e);
        }
        System.out.println();   // blank line before the next message
    }

    // Gives each elevator 2 stops, then moves them all in one loop until every stop is reached.
    private static void testBuilding() throws InterruptedException {
        System.out.println("Building:");
        Building b = new Building();
        b.getElevator(1).addStop(10);
        b.getElevator(1).addStop(3);
        b.getElevator(2).addStop(20);
        b.getElevator(2).addStop(5);
        b.getElevator(3).addStop(15);
        b.getElevator(3).addStop(8);
        b.getElevator(4).addStop(30);
        b.getElevator(4).addStop(2);
        b.printStatus();

        // One loop moves all elevators in turns, so if one had to wait (doors open, etc.)
        // the others would freeze too. That's why next we give each elevator its own thread.
        while (anyStops(b)) {
            for (Elevator e : b.getElevators()) {
                e.moveOneStep();
            }
            Thread.sleep(Config.FLOOR_TRAVEL_MS);   // one "tick" = every elevator moves one floor
            b.printStatus();
        }
        System.out.println();   // blank line before the next message
    }

    // True if at least one elevator still has a stop to reach.
    private static boolean anyStops(Building b) {
        for (Elevator e : b.getElevators()) {
            if (e.hasStops()) {
                return true;
            }
        }
        return false;
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
