package elevator;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Dispatcher implements Runnable
{
    private final List<Elevator> elevators;
    private final BlockingQueue<Request> requests = new LinkedBlockingQueue<>();

    public Dispatcher(List<Elevator> elevators)
    {
        this.elevators = elevators;
    }

    public void submit(Request r)
    {
        System.out.println("New request: floor " + r.floor + ", direction " + r.direction + ", id " + r.id);
        requests.offer(r);
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                Request r = requests.take();
                Elevator best = null;
                int bestDist = Integer.MAX_VALUE;

                for (Elevator e : elevators)
                {
                    int dist = Math.abs(e.getCurrentFloor() - r.floor);
                    if (dist < bestDist)
                    {
                        best = e;
                        bestDist = dist;
                    }
                }

                System.out.println("Dispatcher assigned elevator " + best.getId() + " to request " + r.id);
                best.addDestination(r.floor);
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
}
