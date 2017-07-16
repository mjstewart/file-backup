package fileBackup.backupExecution;

/**
 * The status of a backup operation.
 *
 * Created by matt on 01-Jul-17.
 */
public enum FileBackupStatus {
    SUCCESS("Success"),
    FAILURE("Failed");

    private String description;

    FileBackupStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
