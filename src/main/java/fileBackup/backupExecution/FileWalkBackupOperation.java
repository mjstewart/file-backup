package fileBackup.backupExecution;

import fileBackup.fileAnalysis.FileAccessError;

import java.util.ArrayList;
import java.util.List;

/**
 * When a backup operation is performed by walking the file tree, it is possible the file cannot be visited which
 * never allows a {@code BackupOperation} to be performed. To account for such failure conditions, 2 lists are kept to keep
 * track of which {@code BackupOperation}s were run plus any {@code FileAccessError}s caused during the tree walking
 * process which can be used to ensure appropriate recovery steps can be taken.
 *
 * Created by matt on 04-Jul-17.
 */
public class FileWalkBackupOperation {
    private List<BackupOperation> backupOperations;
    private List<FileAccessError> fileAccessErrors;

    public FileWalkBackupOperation() {
        this.backupOperations = new ArrayList<>();
        this.fileAccessErrors = new ArrayList<>();
    }

    public List<BackupOperation> getBackupOperations() {
        return backupOperations;
    }

    public List<FileAccessError> getFileAccessErrors() {
        return fileAccessErrors;
    }

    public void addBackupOperation(BackupOperation backupOperation) {
        backupOperations.add(backupOperation);
    }

    public void addFileAccessError(FileAccessError fileAccessError) {
        fileAccessErrors.add(fileAccessError);
    }
}
