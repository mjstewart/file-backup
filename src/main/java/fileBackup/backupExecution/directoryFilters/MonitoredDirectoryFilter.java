package fileBackup.backupExecution.directoryFilters;

import fileBackup.fileAnalysis.FilePathInfo;

import java.nio.file.Path;
import java.util.Set;

/**
 * Live monitoring collects paths to directories which are deemed to be active, where active is defined as any CRUD
 * operation being performed on or within a directory. Instances of {@code AbstractFileCollector} use this filter
 * to determine if they can skip an entire directory within the preVisitDirectory step during a file walk.
 *
 * Created by matt on 12-Jul-17.
 */
public class MonitoredDirectoryFilter implements DirectoryFilter {

    private Set<Integer> activeHashCodes;
    private FilePathInfo filePathInfo;

    public MonitoredDirectoryFilter(Set<Integer> activeHashCodes, FilePathInfo filePathInfo) {
        this.activeHashCodes = activeHashCodes;
        this.filePathInfo = filePathInfo;
    }

    @Override
    public boolean isActive(Path path) {
        return activeHashCodes.contains(filePathInfo.getPathMappingStrategy().getUnmappedHashCode(path));
    }
}
