package elevator;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Elevator implements Runnable, Serializable {
    private final int id;
    private int currentFloor;
    private Direction direction;
    private final Queue<Integer> destinations;
    private transient boolean running;

    public Elevator(int id, int startFloor) {
        this.id = id;
        this.currentFloor = startFloor;
        this.direction = Direction.IDLE;
        this.destinations = new LinkedList<>();
        this.running = true;
    }

    public synchronized void addDestination(int floor) {
        destinations.offer(floor);
        notifyAll();
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    public synchronized int getQueueSize() {
        return destinations.size();
    }

    public synchronized void stopElevator() {
        running = false;
        notifyAll();
    }

    private void moveToFloor(int floor) throws InterruptedException {
        while (currentFloor != floor) {
            if (currentFloor < floor) {
                currentFloor++;
                direction = Direction.UP;
            } else {
                currentFloor--;
                direction = Direction.DOWN;
            }
            System.out.println("Elevator " + id + " at floor " + currentFloor);
            Thread.sleep(200);
        }
        System.out.println("Elevator " + id + " doors open");
        Thread.sleep(200);
        System.out.println("Elevator " + id + " doors closed");
        direction = Direction.IDLE;
    }

    @Override
    public void run() {
        while (true) {
            int nextFloor;
            synchronized (this) {
                while (destinations.isEmpty() && running) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) {}
                }
                if (!running && destinations.isEmpty()) break;
                nextFloor = destinations.poll();
            }
            try {
                moveToFloor(nextFloor);
            } catch (InterruptedException ignored) {}
        }
    }

    public synchronized void restoreTransient() {
        running = true;
    }
}
