package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.fileAnalysis.FileChangeRecord;
import io.vavr.control.Try;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * {@code CopyFileTask} copies or replaces a single file and represents a backup stage within a
 * {@code BackupTaskExecutionPipeline}.
 *
 * Created by matt on 02-Jul-17.
 */
public class CopyFileTask extends SingleBackupTask {
    private CopyFileTask(FileChangeRecord record) {
        super(record, "CopyFileTask: Copy/Replace file from current path to backup path");
    }

    public static CopyFileTask of(FileChangeRecord record) {
        return new CopyFileTask(record);
    }

    @Override
    public BackupTaskResult execute() {
        if (record.getBackupPath().getParent() == null || !record.getBackupPath().getParent().toFile().exists()) {
            return BackupTaskResult.failure(record, this, "No parent directory exists, try creating parent directory hierarchy first");
        }

        if (!record.getCurrentWorkingPath().isPresent()) {
            return BackupTaskResult.failure(record, this, "No current path");
        }

        Path currentWorkingPath = record.getCurrentWorkingPath().get();
        return Try.of(() -> Files.copy(currentWorkingPath, record.getBackupPath(), COPY_ATTRIBUTES, REPLACE_EXISTING))
                .map(path -> BackupTaskResult.success(record, this))
                .getOrElseGet(ex -> Match(ex).of(
                        Case($(instanceOf(FileAlreadyExistsException.class)), this::onFileAlreadyExistsException),
                        Case($(instanceOf(DirectoryNotEmptyException.class)), this::onDirectoryNotEmptyException),
                        Case($(), this::onException)
                ));
    }

    private BackupTaskResult onFileAlreadyExistsException(Throwable e) {
        String error = "Target file " + record.getBackupPath() + " already exists, ensure REPLACE_EXISTING attributes are enabled";
        return BackupTaskResult.failure(record, this, error);
    }

    private BackupTaskResult onDirectoryNotEmptyException(Throwable e) {
        String error = "Cannot replace the file since " + record.getBackupPath() + " is a non empty directory";
        return BackupTaskResult.failure(record, this, error);
    }
}
