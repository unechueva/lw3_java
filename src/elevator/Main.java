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

        while (true)
        {
            String s = sc.nextLine();
            if (s.equals("exit")) break;
            try
            {
                String[] p = s.split(" ");
                int floor = Integer.parseInt(p[0]);
                Direction d = p[1].equalsIgnoreCase("up") ? Direction.UP : Direction.DOWN;
                dispatcher.submit(new Request(floor, d, reqId++));
            }
            catch (Exception ignored)
            {
            }
        }
    }
}
