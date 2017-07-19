package ui.tasks;

import fileBackup.backupExecution.directoryFilters.DirectoryFilter;
import fileBackup.fileAnalysis.*;
import io.vavr.control.Either;
import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Scan the file system in both directions (current to backup collecting modified files) and (backup to current collecting
 * deleted files). Both tasks are executed concurrently and the results are combined together and returned within
 * a {@code FileSystemScanResult}.
 *
 * Created by matt on 05-Jul-17.
 */
public class FileCollectorTask extends Task<FileSystemScanResult> {

    private FilePathInfo filePathInfo;
    private DirectoryFilter directoryFilter;

    /**
     *
     * @param filePathInfo The {@code FilePathInfo}.
     * @param directoryFilter Passes through to the {@code AbstractFileCollector}s.
     */
    public FileCollectorTask(FilePathInfo filePathInfo, DirectoryFilter directoryFilter) {
        this.filePathInfo = filePathInfo;
        this.directoryFilter = directoryFilter;
    }

    @Override
    protected FileSystemScanResult call() throws Exception {
        ExecutorService executorService = FileBackupExecutorService.getInstance().get();

        CompletableFuture<Either<FileAccessError, ModifiedFileWalkerResult>> scanModifiedFiles =
                CompletableFuture.supplyAsync(() -> new ModifiedFileCollector(filePathInfo, directoryFilter).getFiles(), executorService);

        CompletableFuture<Either<FileAccessError, DeletedFileWalkerResult>> scanDeletedFiles =
                CompletableFuture.supplyAsync(() -> new DeletedFileCollector(filePathInfo, directoryFilter).getFiles(), executorService);

        return scanModifiedFiles.thenCombineAsync(scanDeletedFiles, FileSystemScanResult::new, executorService)
                .exceptionally(throwable -> new FileSystemScanResult("FileCollectorTask: Error attempting to scan file system"))
                .get();
    }
}
