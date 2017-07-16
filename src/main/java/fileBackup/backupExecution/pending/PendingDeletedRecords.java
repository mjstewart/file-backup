package fileBackup.backupExecution.pending;

import fileBackup.fileAnalysis.FileChangeRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the {@code FileChangeRecord}s pending deletion.
 *
 * Created by matt on 08-Jul-17.
 */
public class PendingDeletedRecords {
    private List<FileChangeRecord> deletedFiles;

    private PendingDeletedRecords(List<FileChangeRecord> deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    public static PendingDeletedRecords of(List<FileChangeRecord> deletedFiles) {
        return new PendingDeletedRecords(deletedFiles);
    }

    /**
     * Creating an empty list is safe as no operations will be performed on an empty list when passed to a
     * {@code BackupExecutor} method. This avoids needing to deal with {@code Optional} when a List will do the same thing.
     *
     * @return Create an instance with an empty list.
     */
    public static PendingDeletedRecords ofEmpty() {
        return new PendingDeletedRecords(new ArrayList<>());
    }

    public List<FileChangeRecord> getDeletedFiles() {
        return deletedFiles;
    }
}
