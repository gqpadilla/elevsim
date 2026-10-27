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
        testQueue();
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

    // Starts the elevators with no stops, then the main thread hands out stops over time (like a dispatcher would).
    private static void testBuilding() throws InterruptedException {
        System.out.println("Building:");
        Building b = new Building();
        b.printStatus();
        b.startAll();

        Thread.sleep(2000);     // arbitrary: 2 s of silence shows idle elevators wait instead of looping
        System.out.println("main: stop 8 for elevator 1");
        b.getElevator(1).addStop(8);
        Thread.sleep(1000);     // arbitrary gap, so the stops arrive while elevator 1 is already moving
        System.out.println("main: stop 15 for elevator 2, stop 4 for elevator 3");
        b.getElevator(2).addStop(15);
        b.getElevator(3).addStop(4);
        Thread.sleep(2000);
        System.out.println("main: stop 12 for elevator 4");
        b.getElevator(4).addStop(12);
        Thread.sleep(2000);
        System.out.println("main: stop 2 for elevator 1");
        b.getElevator(1).addStop(2);    // elevator 1 is idle at 8 by now, this wakes it up again
        Thread.sleep(5000);     // 2+1+2+2+5 = 12 s total, every trip above is done by about 10.5 s

        b.stopAll();
        System.out.println("all elevator threads finished");
    }

    // Producer thread adds 5 requests, consumer thread takes and prints them as they arrive.
    private static void testQueue() throws InterruptedException {
        System.out.println();
        System.out.println("Queue:");
        final RequestQueue queue = new RequestQueue();

        Thread producer = new Thread(new Runnable() {
            @Override
            public void run() {
                int[][] trips = { {5, 32}, {40, 3}, {1, 50}, {12, 7}, {22, 45} };  // arbitrary sample trips
                try {
                    for (int[] t : trips) {
                        Request r = new Request(0, t[0], t[1]);
                        System.out.println("  added " + r);   // print before add(), so "added" always shows before "took"
                        queue.add(r);
                        Thread.sleep(700);  // arbitrary gap between requests, so each take() has to wait
                    }
                } catch (InterruptedException e) {
                    // not interrupted in this test, nothing to clean up
                }
            }
        }, "Producer");

        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request r = queue.take();
                    while (r != null) {     // null means the queue is closed and empty
                        System.out.println("  took " + r);
                        r = queue.take();
                    }
                } catch (InterruptedException e) {
                    // not interrupted in this test, nothing to clean up
                }
                System.out.println("  consumer done");
            }
        }, "Consumer");

        producer.start();
        consumer.start();
        producer.join();    // wait until all 5 requests are added
        queue.close();      // then tell the consumer no more are coming
        consumer.join();    // wait until it has taken everything and ended
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
