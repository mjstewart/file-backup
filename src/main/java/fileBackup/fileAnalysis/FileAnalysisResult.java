package fileBackup.fileAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains statistics used during scanning the file system for new backup tasks.
 *
 * Created by matt on 02-Jul-17.
 */
public class FileAnalysisResult {
    private List<FileAccessError> fileAccessErrors;
    private List<FileChangeRecord> fileChangeRecords;
    private long totalFilesScanned = 0;
    private long totalDirectoriesScanned = 0;

    public FileAnalysisResult() {
        fileChangeRecords = new ArrayList<>();
        fileAccessErrors = new ArrayList<>();
    }

    public List<FileChangeRecord> getFileChangeRecords() {
        return fileChangeRecords;
    }

    public List<FileAccessError> getFileAccessErrors() {
        return fileAccessErrors;
    }

    public long getTotalFilesScanned() {
        return totalFilesScanned;
    }

    public long getTotalDirectoriesScanned() {
        return totalDirectoriesScanned;
    }

    public void addFileChangeRecord(FileChangeRecord fileChangeRecord) {
        fileChangeRecords.add(fileChangeRecord);
    }

    public void addFileError(FileAccessError fileAccessError) {
        fileAccessErrors.add(fileAccessError);
    }

    public void incrementTotalFilesScanned() {
        totalFilesScanned++;
    }

    public void incrementTotalDirectoriesScanned() {
        totalDirectoriesScanned++;
    }
}
