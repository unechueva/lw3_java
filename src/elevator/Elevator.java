package elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator implements Runnable
{
    private final int id;
    private volatile int currentFloor = 1;
    private volatile Direction direction = Direction.IDLE;
    private volatile ElevatorState state = ElevatorState.IDLE;
    private final BlockingQueue<Integer> internalQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean running = true;

    public Elevator(int id)
    {
        this.id = id;
    }

    public boolean tryReserve(long timeoutMs) throws InterruptedException
    {
        return lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public void lock()
    {
        lock.lock();
    }

    public void releaseReserve()
    {
        if (lock.isHeldByCurrentThread()) lock.unlock();
    }

    public void addDestination(int floor)
    {
        internalQueue.offer(floor);
        System.out.println("Elevator " + id + " received new destination: " + floor);
    }

    public List<Integer> snapshotInternalQueue()
    {
        return new ArrayList<>(internalQueue);
    }

    public void clearInternalQueue()
    {
        internalQueue.clear();
    }

    public void stopElevator()
    {
        running = false;
        Thread.currentThread().interrupt();
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

    public String statusString()
    {
        return "Elevator " + id + " floor=" + currentFloor + " state=" + state + " dir=" + direction + " queue=" + internalQueue.size();
    }

    @Override
    public void run()
    {
        try
        {
            while (running)
            {
                Integer next;
                try
                {
                    next = internalQueue.take();
                }
                catch (InterruptedException ex)
                {
                    break;
                }
                System.out.println("Elevator " + id + " moving to " + next);

                if (next > currentFloor) direction = Direction.UP;
                else if (next < currentFloor) direction = Direction.DOWN;
                else direction = Direction.IDLE;

                state = ElevatorState.MOVING;

                while (currentFloor != next && running)
                {
                    try
                    {
                        Thread.sleep(400);
                    }
                    catch (InterruptedException ex)
                    {
                        running = false;
                        break;
                    }
                    if (direction == Direction.UP) currentFloor++;
                    else if (direction == Direction.DOWN) currentFloor--;
                    System.out.println("Elevator " + id + " at floor " + currentFloor);
                }

                if (!running) break;

                state = ElevatorState.DOORS_OPEN;
                System.out.println("Elevator " + id + " doors open");
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException ex)
                {
                    running = false;
                    break;
                }

                state = ElevatorState.IDLE;
                direction = Direction.IDLE;
                System.out.println("Elevator " + id + " doors closed");
            }
        }
        finally
        {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
