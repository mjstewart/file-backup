package ui.tasks;

import fileBackup.backupExecution.*;
import fileBackup.backupExecution.completed.CompletedDeletionBackup;
import fileBackup.backupExecution.completed.CompletedModifiedFilesBackup;
import fileBackup.backupExecution.pending.PendingDeletedRecords;
import fileBackup.backupExecution.pending.PendingModifiedRecords;
import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Executes the backup by firstly backing up the modified files followed by performing any deletions.
 *
 * <p>I feel its best to avoid running both operations in parallel because {@code BackupExecutor.backupModifiedFiles}
 * will be changing files on the backup drive and {@code BackupExecutor.deleteFiles} will be deleting files on the
 * backup drive. While these won't be the same files as the {@code FileChangeRecord} contains exact paths, it still
 * feels like it should be done sequentially to prevent possible random bugs for accessing the same file system at
 * the same time.</p>
 *
 * Created by matt on 08-Jul-17.
 */
public class BackupExecutionTask extends Task<BackupExecutionResult> {

    private PendingModifiedRecords pendingModifiedRecords;
    private PendingDeletedRecords pendingDeletedRecords;

    public BackupExecutionTask(PendingModifiedRecords pendingModifiedRecords, PendingDeletedRecords pendingDeletedRecords) {
        this.pendingModifiedRecords = pendingModifiedRecords;
        this.pendingDeletedRecords = pendingDeletedRecords;
    }

    @Override
    protected BackupExecutionResult call() throws Exception {
        ExecutorService executorService = FileBackupExecutorService.getInstance().get();

        return CompletableFuture.supplyAsync(() -> BackupExecutor.backupModifiedFiles(pendingModifiedRecords), executorService)
                .thenComposeAsync(modifiedBackupOperations -> applyDeleteAndCombine(modifiedBackupOperations, pendingDeletedRecords), executorService)
                .exceptionally(this::onException)
                .get();
    }

    /**
     * Creates a {@code CompletableFuture} which runs the delete files backup executor and packages the result of
     * the previous stage into a single type ready for the caller to receive.
     *
     * @param completedModifiedFilesBackup {@code CompletedModifiedFilesBackup} from the previous step.
     * @param pendingDeletedRecords The records requiring deletion.
     * @return The {@code CompletableFuture}.
     */
    private CompletableFuture<BackupExecutionResult> applyDeleteAndCombine(CompletedModifiedFilesBackup completedModifiedFilesBackup,
                                                                           PendingDeletedRecords pendingDeletedRecords) {
        return CompletableFuture.supplyAsync(() -> {
            CompletedDeletionBackup completedDeletionBackup = BackupExecutor.deleteFiles(pendingDeletedRecords);
            return BackupExecutionResult.success(completedModifiedFilesBackup, completedDeletionBackup);
        });
    }

    private BackupExecutionResult onException(Throwable t) {
        return BackupExecutionResult.failure("BackupExecutionTask: Error attempting to execute backup, " +
                "re-run backup to view files that were not backed up");
    }
}
