package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.fileAnalysis.FileChangeRecord;
import io.vavr.control.Try;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * {@code DeleteFileTask} deletes a single file and represents a backup stage within a {@code BackupTaskExecutionPipeline}.
 * <p>
 * Created by matt on 03-Jul-17.
 */
public class DeleteFileTask extends SingleBackupTask {

    private DeleteFileTask(FileChangeRecord record) {
        super(record, "DeleteFileTask: Delete single backup file at " + record.getBackupPath());
    }

    public static DeleteFileTask of(FileChangeRecord record) {
        return new DeleteFileTask(record);
    }

    @Override
    public BackupTaskResult execute() {
        Try<Boolean> tryDelete = Try.of(() -> {
            if (!Files.isWritable(record.getBackupPath())) {
                throw new Exception("Invalid file permissions - Not writable for " + record.getBackupPath());
            }
            return Files.deleteIfExists(record.getBackupPath());
        });

        return tryDelete
                .map(deleted -> deleted ? BackupTaskResult.success(record, this) : onNonExistingFile())
                .getOrElseGet(ex -> Match(ex).of(
                        Case($(instanceOf(DirectoryNotEmptyException.class)), this::onDirectoryNotEmptyException),
                        Case($(), e -> BackupTaskResult.failure(record, this, e.getMessage())),
                        Case($(instanceOf(SecurityException.class)), this::onSecurityException)
                ));
    }

    private BackupTaskResult onNonExistingFile() {
        String error = "Failed to delete backup file " + record.getBackupPath() + " as it does not exist";
        return BackupTaskResult.failure(record, this, error);
    }

    private BackupTaskResult onDirectoryNotEmptyException(Throwable e) {
        String error = "Cannot delete directory as it is not empty";
        return BackupTaskResult.failure(record, this, error);
    }
}
