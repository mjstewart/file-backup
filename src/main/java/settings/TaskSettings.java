package settings;

import java.util.List;

/**
 * Created by matt on 06-Jul-17.
 */
public class TaskSettings {
    private List<TaskSetting> tasks;

    public TaskSettings(List<TaskSetting> tasks) {
        this.tasks = tasks;
    }

    public TaskSettings() {
    }

    public List<TaskSetting> getTasks() {
        return tasks;
    }
}
