package fileBackup.fileAnalysis;

/**
 * Used by {@code ModifiedFileCollector} to collect statistics for understanding the types of files analysed during
 * the backup scanning process.
 *
 * Created by matt on 30-Jun-17.
 */
public class ModifiedFileWalkerResult extends FileAnalysisResult {
    private long totalFilesModified = 0;
    private long totalFilesUnmodified = 0;
    private long totalNewFiles = 0;
    private long totalNewDirectories = 0;

    public ModifiedFileWalkerResult() {
        super();
    }

    public long getTotalFilesModified() {
        return totalFilesModified;
    }

    public long getTotalFilesUnmodified() {
        return totalFilesUnmodified;
    }

    public long getTotalNewFiles() {
        return totalNewFiles;
    }

    public long getTotalNewDirectories() {
        return totalNewDirectories;
    }

    public void incrementTotalFilesModified() {
        totalFilesModified++;
    }

    public void incrementTotalFilesUnmodified() {
        totalFilesUnmodified++;
    }

    public void incrementTotalNewFiles() {
        totalNewFiles++;
    }

    public void incrementTotalNewDirectories() {
        totalNewDirectories++;
    }
}
