package fileBackup.backupExecution.completed;

/**
 * The results from executing a deletion backup which can contain single file deletion operations as well as
 * complete directory deletions. There are 2 separate types because the behaviour of executing a single file deletion
 * vs a complete directory deletion are different.
 *
 * Created by matt on 04-Jul-17.
 */
public class CompletedDeletionBackup {
    private CompletedSingleFileDeletions completedSingleFileDeletions;
    private CompletedDirectoryDeletions completedDirectoryDeletions;

    private CompletedDeletionBackup(CompletedSingleFileDeletions completedSingleFileDeletions,
                                    CompletedDirectoryDeletions completedDirectoryDeletions) {
        this.completedSingleFileDeletions = completedSingleFileDeletions;
        this.completedDirectoryDeletions = completedDirectoryDeletions;
    }

    public static CompletedDeletionBackup of(CompletedSingleFileDeletions completedSingleFileDeletions,
                                             CompletedDirectoryDeletions completedDirectoryDeletions) {
        return new CompletedDeletionBackup(completedSingleFileDeletions, completedDirectoryDeletions);
    }

    public CompletedSingleFileDeletions getCompletedSingleFileDeletions() {
        return completedSingleFileDeletions;
    }

    public CompletedDirectoryDeletions getCompletedDirectoryDeletions() {
        return completedDirectoryDeletions;
    }
}
