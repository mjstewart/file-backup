package ui.tasks;

import fileBackup.fileAnalysis.FileAccessError;
import fileBackup.monitoring.DirectoryWatcher;
import io.vavr.control.Either;
import javafx.concurrent.Task;

/**
 * Registers all directories to be watched.
 *
 * Created by matt on 13-Jul-17.
 */
public class DirectoryWatcherRegistrationTask extends Task<Either<FileAccessError, DirectoryWatcher>> {

    private DirectoryWatcher directoryWatcher;

    public DirectoryWatcherRegistrationTask(DirectoryWatcher directoryWatcher) {
        this.directoryWatcher = directoryWatcher;
    }

    @Override
    protected Either<FileAccessError, DirectoryWatcher> call() throws Exception {
        return directoryWatcher.create();
    }
}
