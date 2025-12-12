package elevator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Dispatcher implements Runnable
{
    private final List<Elevator> elevators;
    private final BlockingQueue<Request> requests = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public Dispatcher(List<Elevator> elevators)
    {
        this.elevators = elevators;
    }

    public void submit(Request r)
    {
        System.out.println("New request: floor " + r.floor + ", direction " + r.direction + ", id " + r.id);
        requests.offer(r);
    }

    public List<Request> snapshotPendingRequests()
    {
        return new ArrayList<>(requests);
    }

    public void clearPendingRequests()
    {
        requests.clear();
    }

    @Override
    public void run()
    {
        try
        {
            while (running)
            {
                Request r;
                try
                {
                    r = requests.take();
                }
                catch (InterruptedException ex)
                {
                    break;
                }

                Elevator chosen = null;

                for (Elevator e : elevators)
                {
                    try
                    {
                        if (e.tryReserve(50))
                        {
                            chosen = e;
                            break;
                        }
                    }
                    catch (InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (chosen == null)
                {
                    int bestDist = Integer.MAX_VALUE;
                    for (Elevator e : elevators)
                    {
                        int dist = Math.abs(e.getCurrentFloor() - r.floor);
                        if (dist < bestDist)
                        {
                            chosen = e;
                            bestDist = dist;
                        }
                    }
                    if (chosen != null)
                    {
                        chosenLockAndAssign(chosen, r.floor);
                    }
                }
                else
                {
                    try
                    {
                        chosen.addDestination(r.floor);
                    }
                    finally
                    {
                        chosen.releaseReserve();
                    }
                }

                if (chosen != null)
                {
                    System.out.println("Dispatcher assigned elevator " + chosen.getId() + " to request " + r.id);
                }
                else
                {
                    System.out.println("Dispatcher failed to assign elevator to request " + r.id);
                }
            }
        }
        finally
        {
            // nothing
        }
    }

    private void chosenLockAndAssign(Elevator e, int floor)
    {
        e.lock();
        try
        {
            e.addDestination(floor);
        }
        finally
        {
            e.releaseReserve();
        }
    }

    public void stopDispatcher()
    {
        running = false;
        Thread.currentThread().interrupt();
    }

    public void saveStateToFile(String filename)
    {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename)))
        {
            out.println(elevators.size());
            for (Elevator e : elevators)
            {
                out.println(e.getId() + " " + e.getCurrentFloor() + " " + e.getDirection() + " " + e.getState());
                List<Integer> q = e.snapshotInternalQueue();
                out.print(q.size());
                for (Integer x : q) out.print(" " + x);
                out.println();
            }
            List<Request> pending = snapshotPendingRequests();
            out.println(pending.size());
            for (Request r : pending)
            {
                out.println(r.floor + " " + r.direction + " " + r.id + " " + r.timestamp + " " + r.passengers);
            }
            System.out.println("Dispatcher: state saved to " + filename);
        }
        catch (IOException ex)
        {
            System.out.println("Dispatcher: error saving state");
        }
    }

    public void loadStateFromFile(String filename)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(filename)))
        {
            String line = br.readLine();
            int cnt = Integer.parseInt(line.trim());
            for (int i = 0; i < cnt; i++)
            {
                line = br.readLine();
                String[] p = line.split("\\s+");
                int id = Integer.parseInt(p[0]);
                int floor = Integer.parseInt(p[1]);
                line = br.readLine();
                String[] qparts = line.trim().split("\\s+");
                int qsize = Integer.parseInt(qparts[0]);
                Elevator target = elevators.get(id);
                target.clearInternalQueue();
                for (int j = 0; j < qsize; j++)
                {
                    int f = Integer.parseInt(qparts[1 + j]);
                    target.addDestination(f);
                }
            }
            line = br.readLine();
            int pend = Integer.parseInt(line.trim());
            clearPendingRequests();
            for (int i = 0; i < pend; i++)
            {
                line = br.readLine();
                String[] p = line.split("\\s+");
                int floor = Integer.parseInt(p[0]);
                Direction dir = Direction.valueOf(p[1]);
                long id = Long.parseLong(p[2]);
                long ts = Long.parseLong(p[3]);
                int pass = Integer.parseInt(p[4]);
                submit(new Request(floor, dir, id, ts, pass));
            }
            System.out.println("Dispatcher: state loaded from " + filename);
        }
        catch (IOException ex)
        {
            System.out.println("Dispatcher: error loading state");
        }
    }
}
