package settings;

import org.apache.log4j.RollingFileAppender;

/**
 * Allows log file to be set dynamically.
 *
 * Created by matt on 15-Jul-17.
 */
public class FileBackupRollingFileAppender extends RollingFileAppender {
    public FileBackupRollingFileAppender() {
        setFile(ApplicationSettings.getHibernateLogPath().toString());
    }
}
