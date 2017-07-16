package fileBackup.backupExecution;

import fileBackup.backupExecution.completed.CompletedDeletionBackup;
import fileBackup.backupExecution.completed.CompletedModifiedFilesBackup;

import java.util.Optional;

/**
 * Contains the result of executing a complete backup of modified/new and deleted files.
 *
 * Created by matt on 08-Jul-17.
 */
public class BackupExecutionResult {
    private String error;

    private CompletedModifiedFilesBackup completedModifiedFilesBackup;
    private CompletedDeletionBackup completedDeletionBackup;

    private BackupExecutionResult(CompletedModifiedFilesBackup completedModifiedFilesBackup,
                                  CompletedDeletionBackup completedDeletionBackup, String error) {
        this.completedModifiedFilesBackup = completedModifiedFilesBackup;
        this.completedDeletionBackup = completedDeletionBackup;
        this.error = error;
    }

    public static BackupExecutionResult success(CompletedModifiedFilesBackup completedModifiedFilesBackup,
                                                CompletedDeletionBackup completedDeletionBackup) {
        return new BackupExecutionResult(completedModifiedFilesBackup, completedDeletionBackup, null);
    }

    public static BackupExecutionResult failure(String error) {
        return new BackupExecutionResult(null, null, error);
    }

    /**
     * An error can occur during the {@code CompletableFuture} pipeline in {@code BackupExecutionTask}. If there is
     * no error then {@link #getCompletedModifiedFilesBackup} and {@link #getCompletedDeletionBackup} will return valid results.
     *
     * @return The error.
     */
    public Optional<String> getError() {
        if (error == null) {
            return Optional.empty();
        }
        return Optional.of(error);
    }

    /**
     *
     * @return If {@link #getError} is present this method can return null, otherwise the valid
     * {@code CompletedModifiedFilesBackup} is returneed.
     */
    public CompletedModifiedFilesBackup getCompletedModifiedFilesBackup() {
        return completedModifiedFilesBackup;
    }

    /**
     *
     * @return If {@link #getError} is present this method can return null, otherwise the valid
     * {@code CompletedDeletionBackup} is returneed.
     */
    public CompletedDeletionBackup getCompletedDeletionBackup() {
        return completedDeletionBackup;
    }

}
