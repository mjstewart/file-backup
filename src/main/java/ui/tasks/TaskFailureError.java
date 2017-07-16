package ui.tasks;

/**
 * Contains the error message produced from a {@code Task.setOnFailed} event.
 *
 * Created by matt on 09-Jul-17.
 */
public class TaskFailureError {
    private String error;

    private TaskFailureError(String error) {
        this.error = error;
    }

    public static TaskFailureError of(String error) {
        return new TaskFailureError(error);
    }

    public String getError() {
        return error;
    }
}
