package fileBackup.backupExecution.pending;

import fileBackup.fileAnalysis.FileChangeRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the {@code FileChangeRecord}s pending modification on the backup. These records have a status of
 * {@code FileSystemAction.New} or {@code FileSystemAction.Modify}.
 *
 * Created by matt on 08-Jul-17.
 */
public class PendingModifiedRecords {
    private List<FileChangeRecord> modifiedFiles;

    private PendingModifiedRecords(List<FileChangeRecord> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

    public static PendingModifiedRecords of(List<FileChangeRecord> modifiedFiles) {
        return new PendingModifiedRecords(modifiedFiles);
    }

    /**
     * Creating an empty list is safe as no operations will be performed on an empty list when passed to a
     * {@code BackupExecutor} method. This avoids needing to deal with {@code Optional} when a List will do the same thing.
     *
     * @return Create an instance with an empty list.
     */
    public static PendingModifiedRecords ofEmpty() {
        return new PendingModifiedRecords(new ArrayList<>());
    }

    public List<FileChangeRecord> getModifiedFiles() {
        return modifiedFiles;
    }
}
