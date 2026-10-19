package elevatorsim;

// Entry point of the program. For now it only prints a banner and the config values.
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================");    // just a divider line for the banner
        System.out.println(" Elevator Simulation (Java Threads)");
        System.out.println(" " + Config.NUM_ELEVATORS + " elevators, " + Config.NUM_FLOORS + " floors");
        System.out.println("=================================");
        printConfig();
        testRequests();
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
