package settings;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * dot file config for recurring backup tasks to avoid manual entry.
 *
 * Created by matt on 11-Jul-17.
 */
public class ApplicationSettings {

    public static Path getTasksPath() {
        return Paths.get(System.getProperty("user.home"), ".filebackup", "backup-tasks.json");
    }

    public static Path getDatabasePath() {
        return Paths.get(System.getProperty("user.home"), ".filebackup", "db", "filebackup");
    }

    public static Path getHibernateLogPath() {
        return Paths.get(System.getProperty("user.home"), ".filebackup", "hibernate.log");
    }
}
