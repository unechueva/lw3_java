package elevator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Elevator implements Runnable
{
    private final int id;
    private volatile int currentFloor = 1;
    private volatile Direction direction = Direction.IDLE;
    private volatile ElevatorState state = ElevatorState.IDLE;
    private final BlockingQueue<Integer> internalQueue = new LinkedBlockingQueue<>();

    public Elevator(int id)
    {
        this.id = id;
    }

    public void addDestination(int floor)
    {
        internalQueue.offer(floor);
        System.out.println("Elevator " + id + " received new destination: " + floor);
    }

    public int getId()
    {
        return id;
    }

    public int getCurrentFloor()
    {
        return currentFloor;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public ElevatorState getState()
    {
        return state;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                Integer next = internalQueue.take();
                System.out.println("Elevator " + id + " moving to " + next);

                if (next > currentFloor) direction = Direction.UP;
                else if (next < currentFloor) direction = Direction.DOWN;
                else direction = Direction.IDLE;

                state = ElevatorState.MOVING;

                while (currentFloor != next)
                {
                    Thread.sleep(400);
                    if (direction == Direction.UP) currentFloor++;
                    else if (direction == Direction.DOWN) currentFloor--;
                    System.out.println("Elevator " + id + " at floor " + currentFloor);
                }

                state = ElevatorState.DOORS_OPEN;
                System.out.println("Elevator " + id + " doors open");
                Thread.sleep(500);

                state = ElevatorState.IDLE;
                direction = Direction.IDLE;
                System.out.println("Elevator " + id + " doors closed");
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
}
