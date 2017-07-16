package fileBackup.backupExecution.completed;

import fileBackup.backupExecution.BackupOperation;

import java.util.List;

/**
 * Contains the result for executing single file deletions.
 *
 * Created by matt on 09-Jul-17.
 */
public class CompletedSingleFileDeletions {
    private List<BackupOperation> singleFileDeletions;

    private CompletedSingleFileDeletions(List<BackupOperation> singleFileDeletions) {
        this.singleFileDeletions = singleFileDeletions;
    }

    public static CompletedSingleFileDeletions of(List<BackupOperation> singleFileDeletions) {
        return new CompletedSingleFileDeletions(singleFileDeletions);
    }

    public List<BackupOperation> getSingleFileDeletions() {
        return singleFileDeletions;
    }
}
