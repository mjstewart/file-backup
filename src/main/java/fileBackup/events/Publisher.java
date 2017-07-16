package fileBackup.events;

/**
 * Created by matt on 11-Jul-17.
 */
public interface Publisher<T> {
    void addSubscriber(Subscriber<T> subscriber);
    void removeSubscriber(Subscriber<T> subscriber);
}
