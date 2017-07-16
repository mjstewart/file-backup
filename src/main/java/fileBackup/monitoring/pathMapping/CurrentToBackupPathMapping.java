package fileBackup.monitoring.pathMapping;

/**
 * Created by matt on 15-Jul-17.
 */

import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.monitoring.persistence.WatchedFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * For both {@code ModifiedFileCollector} and {@code DeletedFileCollector} to work correctly, both current and
 * backup paths need to be marked as active.
 *
 * <p>For example, when the {@code WatchService} receives a new event for path "C:\Users\me\Desktop\project\important",
 * the backup path needs to be created for "F:\project\important" to allow the {@code DeletedFileCollector} to run.
 * Without saving the backup path, since {@code DeletedFileCollector} reads from the backup drive and looks back
 * at the current working path, no directories are scanned since its looking up to see if
 * "F:\project\important" is in the {@code Set} of active directory hashCodes but only the current drive paths
 * were saved which means only the {@code ModifiedFileCollector} is run.</p>
 */
public class CurrentToBackupPathMapping {
    private List<WatchedFile> mappedFiles;

    public CurrentToBackupPathMapping(Path currentPath, FilePathInfo filePathInfo) {
        this.mappedFiles = new ArrayList<>();

        PathMappingStrategy strategy = filePathInfo.getPathMappingStrategy();
        WatchedFile currentFile = new WatchedFile(strategy.getUnmappedPathString(currentPath), strategy.getUnmappedHashCode(currentPath));
        mappedFiles.add(currentFile);

        Path backupPath = filePathInfo.fromCurrentToBackupPath(currentPath);
        WatchedFile backupFile = new WatchedFile(strategy.getUnmappedPathString(backupPath), strategy.getUnmappedHashCode(backupPath));
        mappedFiles.add(backupFile);
    }

    public List<WatchedFile> getMappedFiles() {
        return mappedFiles;
    }
}