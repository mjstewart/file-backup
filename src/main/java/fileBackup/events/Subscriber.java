package fileBackup.events;

/**
 * Created by matt on 11-Jul-17.
 */
public interface Subscriber<T> {
    void update(T item);
}
