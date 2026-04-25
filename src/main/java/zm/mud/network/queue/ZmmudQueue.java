package zm.mud.network.queue;

public interface ZmmudQueue<T> {
    void put(T b);
    T take();
}
