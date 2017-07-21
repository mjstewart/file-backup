package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.fileAnalysis.FileChangeRecord;
import io.vavr.control.Try;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

/**
 * {@code CreateMissingParentDirectoriesTask} creates all missing parent directories on the backup volume beginning
 * from the supplied {@code FileChangeRecord.getBackupPath().getParent()}.
 *
 * <p>Once the parent directories are created, the {@code CopyFileTask} should be executed next in the pipeline
 * as it depends on parent directories existing.</p>
 *
 * Created by matt on 02-Jul-17.
 */
public class CreateMissingParentDirectoriesTask extends SingleBackupTask {

    private CreateMissingParentDirectoriesTask(FileChangeRecord record) {
        super(record, "CreateMissingParentDirectoriesTask: Create all non existing parent directories on backup volume for " + record.getBackupPath());
    }

    public static CreateMissingParentDirectoriesTask of(FileChangeRecord record) {
        return new CreateMissingParentDirectoriesTask(record);
    }

    @Override
    public BackupTaskResult execute() {
        return Try.of(() -> Files.createDirectories(record.getBackupPath().getParent()))
                .map(path -> BackupTaskResult.success(record, this))
                .getOrElseGet(ex -> Match(ex).of(
                        Case($(instanceOf(FileAlreadyExistsException.class)), this::onFileAlreadyExistsException),
                        Case($(instanceOf(UnsupportedOperationException.class)), this::onUnsupportedOperationException),
                        Case($(instanceOf(SecurityException.class)), this::onSecurityException),
                        Case($(), this::onException)
                ));
    }

    private BackupTaskResult onFileAlreadyExistsException(Throwable e) {
        String error = "Target directory " + record.getBackupPath() + " already exists";
        return BackupTaskResult.failure(record, this, error);
    }

    private BackupTaskResult onUnsupportedOperationException(Throwable e) {
        String error = "Error setting file attribute";
        return BackupTaskResult.failure(record, this, error);
    }
}
