package elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        int elevatorsCount = 3;

        List<Elevator> elevators = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < elevatorsCount; i++)
        {
            Elevator e = new Elevator(i);
            elevators.add(e);
            Thread t = new Thread(e);
            threads.add(t);
            t.start();
        }

        Dispatcher dispatcher = new Dispatcher(elevators);
        Thread dispatcherThread = new Thread(dispatcher);
        dispatcherThread.start();

        long reqId = 1;

        System.out.println("Commands: <floor> <up|down> | status | save <file> | load <file> | exit");

        while (true)
        {
            String s = sc.nextLine();
            if (s == null) break;
            s = s.trim();
            if (s.length() == 0) continue;
            if (s.equalsIgnoreCase("exit"))
            {
                System.out.println("Shutting down...");
                dispatcher.stopDispatcher();
                dispatcherThread.interrupt();
                for (Thread t : threads) t.interrupt();
                for (int i = 0; i < threads.size(); i++)
                {
                    try
                    {
                        threads.get(i).join(500);
                    }
                    catch (InterruptedException ignored)
                    {
                    }
                }
                break;
            }
            else if (s.equalsIgnoreCase("status"))
            {
                for (Elevator e : elevators) System.out.println(e.statusString());
                continue;
            }
            else if (s.startsWith("save "))
            {
                String[] p = s.split("\\s+", 2);
                dispatcher.saveStateToFile(p[1]);
                continue;
            }
            else if (s.startsWith("load "))
            {
                String[] p = s.split("\\s+", 2);
                dispatcher.loadStateFromFile(p[1]);
                continue;
            }

            try
            {
                String[] p = s.split(" ");
                int floor = Integer.parseInt(p[0]);
                Direction d = p[1].equalsIgnoreCase("up") ? Direction.UP : Direction.DOWN;
                dispatcher.submit(new Request(floor, d, reqId++));
            }
            catch (Exception ex)
            {
                System.out.println("Invalid input. Use: <floor> <up|down> or commands listed.");
            }
        }

        System.out.println("Application terminated");
    }
}
