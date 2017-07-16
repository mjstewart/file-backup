package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.fileAnalysis.FileChangeRecord;

/**
 * {@code SingleBackupTask} represents a single backup stage within a {@code BackupTaskExecutionPipeline}.
 *
 * Created by matt on 04-Jul-17.
 */
public abstract class SingleBackupTask extends BackupTask {

    public SingleBackupTask(FileChangeRecord record, String taskDescription) {
        super(record, taskDescription);
    }

    /**
     * @return {@code BackupTaskResult} from executing this {@code SingleBackupTask}.
     */
    public abstract BackupTaskResult execute();
}
