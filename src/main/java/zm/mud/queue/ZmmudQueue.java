package zm.mud.queue;

public interface ZmmudQueue<T> {
    void put(T b);
    T take();
}
