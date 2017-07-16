package fileBackup.backupExecution.completed;

import fileBackup.backupExecution.BackupOperation;

import java.util.List;

/**
 * Contains the result of executing the modified/new files backup.
 *
 * Created by matt on 08-Jul-17.
 */
public class CompletedModifiedFilesBackup {
    private List<BackupOperation> results;

    private CompletedModifiedFilesBackup(List<BackupOperation> results) {
        this.results = results;
    }

    public static CompletedModifiedFilesBackup of(List<BackupOperation> results) {
        return new CompletedModifiedFilesBackup(results);
    }

    public List<BackupOperation> getResults() {
        return results;
    }
}
