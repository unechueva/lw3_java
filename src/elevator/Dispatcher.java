package elevator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Dispatcher implements Serializable {
    private final List<Elevator> elevators;
    private transient List<Thread> elevatorThreads;

    public Dispatcher(int elevatorsCount, int startFloor) {
        elevators = new ArrayList<>();
        elevatorThreads = new ArrayList<>();
        for (int i = 0; i < elevatorsCount; i++) {
            Elevator e = new Elevator(i, startFloor);
            elevators.add(e);
            Thread t = new Thread(e);
            elevatorThreads.add(t);
            t.start();
        }
    }

    public synchronized void requestElevator(int floor, Direction dir) {
        System.out.println("New request: floor " + floor + ", direction " + dir);
        Elevator best = findBestElevator(floor, dir);
        best.addDestination(floor);
        System.out.println("Dispatcher assigned elevator " + bestId(best) + " to request");
    }

    private Elevator findBestElevator(int floor, Direction dir) {
        Elevator chosen = elevators.get(0);
        int minDistance = Math.abs(chosen.getCurrentFloor() - floor);
        for (Elevator e : elevators) {
            int distance = Math.abs(e.getCurrentFloor() - floor);
            if (distance < minDistance) {
                minDistance = distance;
                chosen = e;
            }
        }
        return chosen;
    }

    private int bestId(Elevator e) {
        return elevators.indexOf(e);
    }

    public synchronized void status() {
        for (Elevator e : elevators) {
            System.out.println("Elevator " + bestId(e) + " floor=" + e.getCurrentFloor() +
                    " state=" + e.getDirection() + " queue=" + e.getQueueSize());
        }
    }

    public synchronized void stopAll() {
        for (Elevator e : elevators) {
            e.stopElevator();
        }
        for (Thread t : elevatorThreads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {}
        }
    }

    public synchronized void restoreElevators() {
        elevatorThreads = new ArrayList<>();
        for (Elevator e : elevators) {
            e.restoreTransient();
            Thread t = new Thread(e);
            elevatorThreads.add(t);
            t.start();
        }
    }

    public void save(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
            System.out.println("Dispatcher: state saved to " + filename);
        } catch (IOException ex) {
            System.out.println("Error saving state: " + ex.getMessage());
        }
    }

    public static Dispatcher load(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            Dispatcher d = (Dispatcher) in.readObject();
            d.restoreElevators();
            System.out.println("Dispatcher: state loaded from " + filename);
            return d;
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Error loading state: " + ex.getMessage());
            return null;
        }
    }
}
