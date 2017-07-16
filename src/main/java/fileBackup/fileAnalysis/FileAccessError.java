package fileBackup.fileAnalysis;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by matt on 30-Jun-17.
 */
public class FileAccessError {
    // Optional path to add more context to the path involved in creating the error.
    private Path path;

    private String reason;

    public FileAccessError(Path path, String reason) {
        this.path = path;
        this.reason = reason;
    }

    public FileAccessError(String reason) {
        this.path = null;
        this.reason = reason;
    }

    /**
     * @return The path involved in creating this error if one exists.
     */
    public Optional<Path> getPath() {
        if (path == null) return Optional.empty();
        return Optional.of(path);
    }

    /**
     * @return The error reason.
     */
    public String getReason() {
        return reason;
    }
}
