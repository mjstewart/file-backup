package ui.tasks;

import fileBackup.monitoring.persistence.FileBackupRepository;
import javafx.concurrent.Task;

/**
 * A potentially time consuming task to query the database for all tables required to determine if a previous
 * monitoring session has been saved. Using a Task provides the runningProperty for free allowing the UI to show
 * feedback without blocking the UI thread.
 *
 * Created by matt on 16-Jul-17.
 */
public class GetAllTablesTask extends Task<AllTablesResult> {
    @Override
    protected AllTablesResult call() throws Exception {
        return new AllTablesResult(FileBackupRepository.getFilePathInfo(),
                FileBackupRepository.getAllFiles(),
                FileBackupRepository.getAllLogMessages());
    }
}
