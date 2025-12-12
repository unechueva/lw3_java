package elevator;

public class Request
{
    public final int floor;
    public final Direction direction;
    public final long id;
    public final long timestamp;
    public final int passengers;

    public Request(int floor, Direction direction, long id)
    {
        this(floor, direction, id, System.currentTimeMillis(), 1);
    }

    public Request(int floor, Direction direction, long id, long timestamp, int passengers)
    {
        this.floor = floor;
        this.direction = direction;
        this.id = id;
        this.timestamp = timestamp;
        this.passengers = passengers;
    }
}
