package ui.tasks;

/**
 * Implemented by any type with resources needing to be shutdown on application close.
 *
 * Created by matt on 11-Jul-17.
 */
public interface Shutdownable {
    void stop();
}
