package fileBackup.backupExecution.completed;

import fileBackup.backupExecution.backupTasks.DeleteDirectoryTaskResult;

import java.util.List;

/**
 * Contains the result of performing a directory deletion.
 *
 * Created by matt on 09-Jul-17.
 */
public class CompletedDirectoryDeletions {
    private List<DeleteDirectoryTaskResult> directoryDeletions;

    private CompletedDirectoryDeletions(List<DeleteDirectoryTaskResult> directoryDeletions) {
        this.directoryDeletions = directoryDeletions;
    }

    public static CompletedDirectoryDeletions of(List<DeleteDirectoryTaskResult> directoryDeletions) {
        return new CompletedDirectoryDeletions(directoryDeletions);
    }

    public List<DeleteDirectoryTaskResult> getDirectoryDeletions() {
        return directoryDeletions;
    }
}
