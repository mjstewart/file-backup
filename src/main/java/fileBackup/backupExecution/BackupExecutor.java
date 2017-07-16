package fileBackup.backupExecution;

import fileBackup.backupExecution.backupTasks.*;
import fileBackup.backupExecution.completed.CompletedDeletionBackup;
import fileBackup.backupExecution.completed.CompletedDirectoryDeletions;
import fileBackup.backupExecution.completed.CompletedModifiedFilesBackup;
import fileBackup.backupExecution.completed.CompletedSingleFileDeletions;
import fileBackup.backupExecution.pending.PendingDeletedRecords;
import fileBackup.backupExecution.pending.PendingModifiedRecords;
import fileBackup.fileAnalysis.FileChangeRecord;
import fileBackup.fileAnalysis.FileSystemAction;
import fileBackup.fileAnalysis.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point to run backup execution tasks.
 *
 * Created by matt on 30-Jun-17.
 */
public class BackupExecutor {

    public static CompletedModifiedFilesBackup backupModifiedFiles(PendingModifiedRecords pendingModifiedRecords) {
        List<BackupOperation> backupOperations = new ArrayList<>();

        for (FileChangeRecord record : pendingModifiedRecords.getModifiedFiles()) {
            if (record.getFileSystemAction() == FileSystemAction.Modify) {
                BackupTaskExecutionPipeline pipeline = BackupTaskExecutionPipeline.of(CopyFileTask.of(record));
                backupOperations.add(BackupOperation.of(record, pipeline));
            }
            if (record.getFileSystemAction() == FileSystemAction.New) {
                BackupTaskExecutionPipeline pipeline = BackupTaskExecutionPipeline
                        .of(CreateMissingParentDirectoriesTask.of(record))
                        .andThen(CopyFileTask.of(record));
                backupOperations.add(BackupOperation.of(record, pipeline));
            }
        }
        return CompletedModifiedFilesBackup.of(backupOperations);
    }

    public static CompletedDeletionBackup deleteFiles(PendingDeletedRecords pendingDeletedRecords) {
        List<BackupOperation> singleFileDeletions = new ArrayList<>();
        List<DeleteDirectoryTaskResult> directoryDeletions = new ArrayList<>();

        for (FileChangeRecord record : pendingDeletedRecords.getDeletedFiles()) {
            if (record.getFileSystemAction() == FileSystemAction.Delete) {
                if (record.getFileType() == FileType.File) {
                    BackupTaskExecutionPipeline pipeline = BackupTaskExecutionPipeline.of(DeleteFileTask.of(record));
                    BackupOperation backupOperation = BackupOperation.of(record, pipeline);
                    singleFileDeletions.add(backupOperation);
                }
                if (record.getFileType() == FileType.Directory) {
                    directoryDeletions.add(DeleteDirectoryTask.of(record).execute());
                }
            }
        }
        return CompletedDeletionBackup.of(CompletedSingleFileDeletions.of(singleFileDeletions),
                CompletedDirectoryDeletions.of(directoryDeletions));
    }
}
