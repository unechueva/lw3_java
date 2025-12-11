package elevator;

public class Request
{
    public final int floor;
    public final Direction direction;
    public final long id;

    public Request(int floor, Direction direction, long id)
    {
        this.floor = floor;
        this.direction = direction;
        this.id = id;
    }
}
