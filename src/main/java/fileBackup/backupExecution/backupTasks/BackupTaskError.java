package fileBackup.backupExecution.backupTasks;

/**
 * Created by matt on 04-Jul-17.
 */
public class BackupTaskError {
    private BackupTask backupTask;
    private String reason;

    public BackupTaskError(BackupTask backupTask, String reason) {
        this.backupTask = backupTask;
        this.reason = reason;
    }

    public BackupTask getBackupTask() {
        return backupTask;
    }

    public String getReason() {
        return reason;
    }
}
