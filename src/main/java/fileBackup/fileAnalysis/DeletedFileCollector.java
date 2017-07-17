package fileBackup.fileAnalysis;

import fileBackup.backupExecution.directoryFilters.DirectoryFilter;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * Performs a read only file system scan starting on the backup drive starting from
 * {@code filePathInfo.getBackupRootPath()} looking for all files and directories not existing on the current drive.
 * The motivation is to view what changes are available first before executing the real backup through the {@code BackupExecutor}.
 *
 * <p>Its works in the opposite direction of the {@code ModifiedFileCollector} and can be run in parallel since
 * it doesn't modify the backup drive.</p>
 *
 * <p>No support for symlinks is needed as {@code DeletedFileCollector} scans the backup drive which has no references
 * to other files given the use case is do the backup on a USB, not the current drive. This is contrasted to the
 * {@code ModifiedFileCollector} where symlinks are supported as its possible the need to create a starting root
 * directory that contains symlinks to all the other directories you want backed up.</p>
 *
 * <p>The strategy for determining if a file that is on the backup drive does not exist on the current drive is
 * first to get the backup path and map it to what its representation would be on the current drive which is achieved using
 * path mapping methods in {@code FilePathInfo}.</p>
 *
 * <p>A {@code FileChangeRecord} is created for each file eligible to be backed up. From here these
 * {@code FileChangeRecord}s can be sent to a {@code BackupExecutor} to perform the actual file changes.</p>
 *
 * <p>Below describes the types of file changes this {@code DeletedFileCollector} looks for.</p>
 *
 * <table border=1 cellpadding=5 summary="">
 * <tr> <th>Rules</th> <th>Description</th> </tr>
 * <tr>
 *   <td>Single file deleted</td>
 *   <td>The backup file does not appear on the current drive indicating it can be deleted from the backup since it
 *   no longer exists on the current drive.</td>
 * </tr>
 * <tr>
 *   <td>Delete entire directory</td>
 *   <td>The backup directory does not appear on the current drive indicating the backup directory and all its contents
 *   can be deleted from the backup drive.</td>
 * </tr>
 * </table>
 *
 * Created by matt on 30-Jun-17.
 */
public class DeletedFileCollector extends AbstractFileCollector<FileAnalysisResult> {

    public DeletedFileCollector(FilePathInfo filePathInfo, DirectoryFilter directoryFilter) {
        super(filePathInfo, directoryFilter);
    }

    public Either<FileAccessError, FileAnalysisResult> getFiles() {
        DeletedFileVisitor deletedFileVisitor = new DeletedFileVisitor();

        Try<Path> tryWalk = Try.of(() -> Files.walkFileTree(filePathInfo.getBackupRootPath(), deletedFileVisitor));
        if (tryWalk.isSuccess()) {
            return Either.right(deletedFileVisitor.fileAnalysisResult);
        }
        return Either.left(new FileAccessError("DeletedFileCollector: unable to walk files due to IO error"));
    }

    private class DeletedFileVisitor implements FileVisitor<Path> {
        // Mutated during tree walking process and must only be accessed after walking has completed.
        private FileAnalysisResult fileAnalysisResult;

        public DeletedFileVisitor() {
            fileAnalysisResult = new FileAnalysisResult();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (dir == null) {
                fileAnalysisResult.addFileError(new FileAccessError("preVisitDirectory: Path argument is null, " +
                        "a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }

            if (!directoryFilter.isActive(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            }

            if (attrs == null) {
                fileAnalysisResult.addFileError(new FileAccessError(dir,"preVisitDirectory: BasicFileAttributes " +
                        "argument is null for " + dir.toString() + " resulting in this path being excluded from backup analysis"));
                return FileVisitResult.CONTINUE;
            }
            fileAnalysisResult.incrementTotalDirectoriesScanned();

            File backupFile = dir.toFile();
            FileTime backupFileLastModified = attrs.lastModifiedTime();
            Path currentWorkingPath = filePathInfo.fromBackupToCurrentPath(dir);
            File currentWorkingFile = currentWorkingPath.toFile();

            /*
             * Backup directory doesn't exist on current working version so the entire backup directory can be marked
             * for deletion. Skip entire directory given the DeleteDirectoryTask will go through and delete
             * directory and all its contents.
             */
            if (!currentWorkingFile.exists()) {
                FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                        .currentWorkingPath(currentWorkingPath)
                        .backupPath(dir)
                        .fileSystemAction(FileSystemAction.Delete)
                        .backupLastModified(backupFileLastModified)
                        .fileType(toFileType(backupFile))
                        .create();

                fileAnalysisResult.addFileChangeRecord(fileChangeRecord);
                return FileVisitResult.SKIP_SUBTREE;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file == null) {
                fileAnalysisResult.addFileError(new FileAccessError("visitFile: Path argument is null, " +
                        "a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            if (attrs == null) {
                fileAnalysisResult.addFileError(new FileAccessError(file,"visitFile: BasicFileAttributes argument " +
                        "is null for " + file.toString() + " resulting in this path being excluded from backup analysis"));
                return FileVisitResult.CONTINUE;
            }

            fileAnalysisResult.incrementTotalFilesScanned();

            FileTime backupFileLastModified = attrs.lastModifiedTime();

            // Map from backup path to current working current path.
            Path currentWorkingPath = filePathInfo.fromBackupToCurrentPath(file);
            File currentWorkingFile = currentWorkingPath.toFile();

            if (!currentWorkingFile.exists()) {
                FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                        .currentWorkingPath(currentWorkingPath)
                        .backupPath(file)
                        .backupLastModified(backupFileLastModified)
                        .fileSystemAction(FileSystemAction.Delete)
                        .fileType(toFileType(file.toFile()))
                        .create();

                fileAnalysisResult.addFileChangeRecord(fileChangeRecord);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (file == null) {
                fileAnalysisResult.addFileError(new FileAccessError("visitFileFailed: Path argument is null, " +
                        "a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            if (exc != null) {
                fileAnalysisResult.addFileError(new FileAccessError(file, exc.getMessage() + ", " + file.toString() +
                        " could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (dir == null) {
                fileAnalysisResult.addFileError(new FileAccessError("postVisitDirectory: Path argument is null, " +
                        "a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            if (exc != null) {
                fileAnalysisResult.addFileError(new FileAccessError(dir, exc.getMessage() + ", " + dir.toString() + " " +
                        "could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
