package fileBackup.backupExecution;

import fileBackup.fileAnalysis.FileChangeRecord;

/**
 * Associates a {@code BackupTaskExecutionPipeline} to a {@code FileChangeRecord}.
 *
 * Created by matt on 04-Jul-17.
 */
public class BackupOperation {
    private FileChangeRecord fileChangeRecord;
    private BackupTaskExecutionPipeline backupTaskExecutionPipeline;

    private BackupOperation(FileChangeRecord fileChangeRecord,
                            BackupTaskExecutionPipeline backupTaskExecutionPipeline) {
        this.fileChangeRecord = fileChangeRecord;
        this.backupTaskExecutionPipeline = backupTaskExecutionPipeline;
    }

    public static BackupOperation of(FileChangeRecord fileChangeRecord,
                                     BackupTaskExecutionPipeline backupTaskExecutionPipeline) {
        return new BackupOperation(fileChangeRecord, backupTaskExecutionPipeline);
    }

    /**
     * @return {@code FileBackupStatus.SUCCESS} only if every requested task submitted to {@code BackupTaskExecutionPipeline}
     * was successfully executed, otherwise {@code FileBackupStatus.FAILURE}
     */
    public FileBackupStatus getFileBackupStatus() {
        return backupTaskExecutionPipeline.allStagesSuccessful() ? FileBackupStatus.SUCCESS : FileBackupStatus.FAILURE;
    }

    public BackupTaskExecutionPipeline getBackupTaskExecutionPipeline() {
        return backupTaskExecutionPipeline;
    }

    public FileChangeRecord getFileChangeRecord() {
        return fileChangeRecord;
    }
}
