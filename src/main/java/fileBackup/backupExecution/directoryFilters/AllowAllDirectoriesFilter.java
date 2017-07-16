package fileBackup.backupExecution.directoryFilters;

import java.nio.file.Path;

/**
 * Manual backup analysis deems all directories as active since every single file must be scanned.
 *
 * Created by matt on 12-Jul-17.
 */
public class AllowAllDirectoriesFilter implements DirectoryFilter {
    @Override
    public boolean isActive(Path path) {
        return true;
    }
}
