package zm.mud.network.queue;

public interface IZmmudQueue<T> {
    void put(T b);
    T take();
}
