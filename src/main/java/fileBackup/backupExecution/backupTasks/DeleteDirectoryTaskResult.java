package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.FileWalkBackupOperation;
import fileBackup.fileAnalysis.FileChangeRecord;
import io.vavr.control.Either;

/**
 * Contains the start root directory information in {@link #getStartingRootRecord} with the success or failure
 * of running the file walk procedure of deleting the entire directory contents in {@link #getResult}.
 *
 * Created by matt on 09-Jul-17.
 */
public class DeleteDirectoryTaskResult {
    private FileChangeRecord startingRootRecord;
    private Either<BackupTaskError, FileWalkBackupOperation> result;

    /**
     * @param startingRootRecord The root record containing the root directory from which the file walking begins to
     *                           delete the entire contents from.
     * @param result The {@code Either} representing the error if the file walk could not begin otherwise contains
     *               the results of completing the file walk.
     */
    public DeleteDirectoryTaskResult(FileChangeRecord startingRootRecord,
                                     Either<BackupTaskError, FileWalkBackupOperation> result) {
        this.startingRootRecord = startingRootRecord;
        this.result = result;
    }

    /**
     * @return The root record containing the root directory from which the file walking begins to
     * delete the entire contents from.
     */
    public FileChangeRecord getStartingRootRecord() {
        return startingRootRecord;
    }

    /**
     * @return The {@code Either} representing the error if the file walk could not begin otherwise contains
     * the results of completing the file walk.
     */
    public Either<BackupTaskError, FileWalkBackupOperation> getResult() {
        return result;
    }

    /**
     * @return {@code true} if there is a {@code FileWalkBackupOperation} present containing the complete results
     * of this directory deletion task.
     */
    public boolean isValid() {
        return result.isRight();
    }
}
