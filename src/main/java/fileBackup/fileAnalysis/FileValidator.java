package fileBackup.fileAnalysis;

import java.nio.file.Path;

/**
 * Created by matt on 07-Jul-17.
 */
public class FileValidator {

    /**
     * Verifies that the supplied {@code Path} has an existing underlying {@code File}.
     *
     * <p>Allows for mocking the file existence check to ensure {@code FilePathInfo} is only constructed in a valid
     * state. Unit tests use fake paths and we don't want to be coupled to the underlying file system but we still
     * need to ensure the integrity of {@code FilePathInfo} using valid existing files.</p>
     *
     * @param path The {@code Path} to check for existence.
     * @return {@code true} if the underlying {@code File} exists.
     */
    public boolean fileExists(Path path) {
        return path.toFile().exists();
    }
}
