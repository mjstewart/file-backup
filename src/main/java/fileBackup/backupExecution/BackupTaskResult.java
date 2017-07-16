package fileBackup.backupExecution;

import fileBackup.fileAnalysis.FileChangeRecord;
import fileBackup.backupExecution.backupTasks.BackupTask;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Represents the result of applying a {@code BackupTask} to the file system
 *
 * Created by matt on 01-Jul-17.
 */
public class BackupTaskResult {

    private FileChangeRecord fileChangeRecord;
    private FileBackupStatus fileBackupStatus;
    private LocalDateTime backupCommitTime;
    private BackupTask backupTask;

    // Optional and can be null since a success contains no error.
    private String error;

    private BackupTaskResult(FileChangeRecord fileChangeRecord,
                             FileBackupStatus fileBackupStatus,
                             BackupTask backupTask,
                             String error) {
        this.fileChangeRecord = fileChangeRecord;
        this.fileBackupStatus = fileBackupStatus;
        this.backupCommitTime = LocalDateTime.now();
        this.backupTask = backupTask;
        this.error = error;
    }

    public static BackupTaskResult success(FileChangeRecord fileChangeRecord, BackupTask backupTask) {
        return new BackupTaskResult(fileChangeRecord, FileBackupStatus.SUCCESS, backupTask, null);
    }

    public static BackupTaskResult failure(FileChangeRecord fileChangeRecord, BackupTask backupTask, String error) {
        return new BackupTaskResult(fileChangeRecord, FileBackupStatus.FAILURE, backupTask, error);
    }

    public FileChangeRecord getFileChangeRecord() {
        return fileChangeRecord;
    }

    public FileBackupStatus getFileBackupStatus() {
        return fileBackupStatus;
    }

    public Optional<String> getErrorReason() {
        if (error == null) {
            return Optional.empty();
        }
        return Optional.of(error);
    }

    public LocalDateTime getBackupCommitTime() {
        return backupCommitTime;
    }

    public BackupTask getBackupTask() {
        return backupTask;
    }

    @Override
    public String toString() {
        if (fileBackupStatus == FileBackupStatus.SUCCESS) {
            return "BackupTaskResult{" +
                    "fileBackupStatus=" + fileBackupStatus +
                    ", backupCommitTime=" + backupCommitTime +
                    ", backupTask=" + backupTask.getTaskDescription() +
                    '}';
        }

        return "BackupTaskResult{" +
                "fileBackupStatus=" + fileBackupStatus +
                ", errorReason=" + getErrorReason() +
                ", backupCommitTime=" + backupCommitTime +
                ", backupTask=" + backupTask.getTaskDescription() +
                '}';
    }
}

