package fileBackup.backupExecution.directoryFilters;

import java.nio.file.Path;

/**
 * Checks if a directory is active which determines whether or not an instance of {@code AbstractFileCollector} should
 * traverse a directory or skip it entirely. Active is defined as any CRUD operation being performed on or
 * within a directory.
 *
 * Created by matt on 12-Jul-17.
 */
public interface DirectoryFilter {
    /**
     * If the supplied {@code Path} is deemed inactive, the entire directory can be pruned otherwise returning
     * {@code true} results in the directory being scanned.
     *
     * @param path The {@code Path} to check.
     * @return {@code true} if the {@code Path} has received new 'activity' otherwise {@code false}.
     */
    boolean isActive(Path path);
}
