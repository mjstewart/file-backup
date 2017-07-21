package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.fileAnalysis.FileChangeRecord;

/**
 * {@code BackupTask} represents a single backup stage within a {@code BackupTaskExecutionPipeline}.
 * <p>
 * Created by matt on 02-Jul-17.
 */
public abstract class BackupTask {
    protected FileChangeRecord record;
    private String taskDescription;

    public BackupTask(FileChangeRecord record, String taskDescription) {
        this.record = record;
        this.taskDescription = taskDescription;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    protected BackupTaskResult onException(Throwable e) {
        return BackupTaskResult.failure(record, this, e.getMessage());
    }

    protected BackupTaskResult onSecurityException(Throwable e) {
        String error = "Security Exception: " + e.getMessage();
        return BackupTaskResult.failure(record, this, error);
    }
}
