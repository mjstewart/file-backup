package fileBackup.monitoring.pathMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Centralises the path mapping strategy for determining if a directory has been modified.
 *
 * <p>For example, when a new event is received by the {@code DirectoryWatcher}, the full file name is given.
 * The strategy adopted is explained in the {@code DirectoryWatcher} docs with this class providing the implementation
 * of mapping a supplied {@code Path} including any file names and transforming it based on whether it is deemed to
 * be a directory determined by the {@code directoryChecker} predicate. Additional methods are provided to get
 * the {@code String} representation for persistence and {@code getMappedHashCode} for querying.</p>
 *
 * <p>The main motivation to centralise this is for isolated testing and to reduce the chance of subtle bugs. Any
 * class that needs to use a {@code Path} goes through these static methods such as {@code AbstractFileCollector}s
 * and {@code DirectoryWatcher}.</p>
 *
 * <p>Unmapped versions are provided as for example {@code MonitoredDirectoryFilter} used by {@code ModifiedFileCollector}
 * only cares about an unmapped path and seeing if its in the {@code Set} of active directory hashCodes. Therefore
 * no mapping to parent directories needs to be done, however a central place to ensure the hashCode is correctly
 * calculated is needed as its easy to use the {@code Path} hashCode which is different from using the {@code Path}s
 * internal strings hashCode.</p>
 *
 * Created by matt on 12-Jul-17.
 */
public class PathMappingStrategy {

    private Predicate<Path> directoryChecker;

    /**
     * @param directoryChecker returns {@code true} if supplied {@code Path} is a directory otherwise {@code false}.
     */
    public PathMappingStrategy(Predicate<Path> directoryChecker) {
        this.directoryChecker = directoryChecker;
    }

    /**
     * @return A new instance with the {@code directoryChecker} set to use {@code Files.isDirectory}.
     */
    public static PathMappingStrategy create() {
        return new PathMappingStrategy(path -> Files.isDirectory(path));
    }

    /**
     * @param path The full unmodified {@code Path}.
     * @return The mapped {@code Path}.
     */
    public Path map(Path path) {
        if (directoryChecker.test(path)) {
            return path;
        }
        return path.getParent();
    }

    /**
     * Do not apply any {@code Path} mappings before calling this method as this method does that internally.
     *
     * @param path The full unmodified {@code Path}.
     * @return The mapped hashCode.
     */
    public int getMappedHashCode(Path path) {
        // toString must be used as its different from using hashCode of the Path instance.
        return map(path).toString().hashCode();
    }

    /**
     * Gets the supplied {@code Path}s internal {@code String} representation.
     *
     * <p>Do not apply any {@code Path} mappings before calling this method as this method does that internally.</p>
     *
     * @param path The full unmodified {@code Path}.
     * @return The {@code String} representation.
     */
    public String getMappedPathString(Path path) {
        return map(path).toString();
    }

    /**
     * Unmapped version of getMappedHashCode.
     *
     * @param path The full unmodified {@code Path}.
     * @return The unmapped hashCode.
     */
    public int getUnmappedHashCode(Path path) {
        // toString must be used as its different from using hashCode of the Path instance.
        return path.toString().hashCode();
    }

    /**
     * Unmapped version of getMappedPathString
     *
     * @param path The full unmodified {@code Path}.
     * @return The {@code String} representation.
     */
    public String getUnmappedPathString(Path path) {
        return path.toString();
    }
}
