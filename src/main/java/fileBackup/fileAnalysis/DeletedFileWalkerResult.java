package fileBackup.fileAnalysis;

/**
 * Used by {@code DeletedFileCollector} to collect statistics for understanding the types of files analysed during
 * the backup scanning process.
 *
 * Created by matt on 30-Jun-17.
 */
public class DeletedFileWalkerResult extends FileAnalysisResult {
    private long totalFilesDeleted = 0;
    private long totalDirectoriesDeleted = 0;

    public long getTotalFilesDeleted() {
        return totalFilesDeleted;
    }

    public long getTotalDirectoriesDeleted() {
        return totalDirectoriesDeleted;
    }

    public void incrementTotalFilesDeleted() {
        this.totalFilesDeleted++;
    }

    public void incrementTotalDirectoriesDeleted() {
        this.totalDirectoriesDeleted++;
    }
}
