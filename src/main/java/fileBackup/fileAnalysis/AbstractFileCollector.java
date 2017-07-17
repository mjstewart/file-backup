package fileBackup.fileAnalysis;

import fileBackup.backupExecution.directoryFilters.DirectoryFilter;
import io.vavr.control.Either;

import java.io.File;
import java.nio.file.Files;

/**
 * @param <R> The type of {@code FileAnalysisResult} used when collecting files.
 */
public abstract class AbstractFileCollector<R extends FileAnalysisResult> {
    protected FilePathInfo filePathInfo;
    protected DirectoryFilter directoryFilter;

    /**
     * @param filePathInfo The {@code FilePathInfo}.
     * @param directoryFilter Determines which directories should be scanned.
     */
    protected AbstractFileCollector(FilePathInfo filePathInfo, DirectoryFilter directoryFilter) {
        this.filePathInfo = filePathInfo;
        this.directoryFilter = directoryFilter;
    }

    protected FileType toFileType(File file) {
        if (file.isFile()) {
            return FileType.File;
        }
        if (Files.isSymbolicLink(file.toPath())) {
            return FileType.Symbolic;
        }
        if (file.isDirectory()) {
            return FileType.Directory;
        }
        return FileType.Unknown;
    }

    /**
     * @return The {@code Either} where left is the error if this method failed otherwise right contains {@code R}
     * which is of type {@code FileAnalysisResult}.
     */
    public abstract Either<FileAccessError, R> getFiles();
}
