package fileBackup.fileAnalysis;

import fileBackup.backupExecution.directoryFilters.DirectoryFilter;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;

/**
 * Performs a read only file system scan starting on the current drive starting from
 * {@code filePathInfo.getCurrentWorkingRootPath()} looking for files new or modified files.
 * The motivation is to view what changes are available first before executing
 * the real backup through the {@code BackupExecutor}.
 *
 * <p>The strategy for comparing files on different drives is to first get the path of the current directory then
 * map the path to what its representation would be on the backup drive which is achieved using
 * path mapping methods in {@code FilePathInfo}.</p>
 *
 * <p>A {@code FileChangeRecord} is created for each file eligible to be backed up. From here these
 * {@code FileChangeRecord}s can be sent to a {@code BackupExecutor} to perform the actual file changes.</p>
 *
 * <p>Below describes the types of file changes this {@code ModifiedFileCollector} looks for.</p>
 *
 * <table border=1 cellpadding=5 summary="">
 * <tr> <th>Rules</th> <th>Description</th> </tr>
 * <tr>
 *   <td>File changed</td>
 *   <td>If the current working file has a different last modified time to the backup file, the backup file is
 *   replaced with the current working file.</td>
 * </tr>
 * <tr>
 *   <td>New file</td>
 *   <td>If the current working file is not located on the backup volume, the new file
 *   is added to the backup volume.</td>
 * </tr>
 * <tr>
 *   <td>Illegal states</td>
 *   <td>If the backup file is newer than the current working file then the backup copy has been tampered with
 *   which is outside the scope of this application to deal with. The assumption is once the initial backup is
 *   performed manually by a human, only this application is used to apply future backups to ensure the backup
 *   correctly remains in sync and updated.</td>
 * </tr>
 * </table>
 *
 * Created by matt on 30-Jun-17.
 */
public class ModifiedFileCollector extends AbstractFileCollector<ModifiedFileWalkerResult> {
    /*
     * If a file hasn't been changed in the last FILE_UNCHANGED_THRESHOLD_SECONDS, count it as unchanged.
     * This is to account for minor variances in lastModifiedTime when copying. See Files.copy docs,
     * it says some precision can be lost when copying and appears to be around 1 or 2 seconds difference.
     * The underlying operating system may also change time stamps when copying based on google research and windows.
     */
    private final static long FILE_UNCHANGED_THRESHOLD_SECONDS = 5;

    public ModifiedFileCollector(FilePathInfo filePathInfo, DirectoryFilter directoryFilter) {
        super(filePathInfo, directoryFilter);
    }

    public Either<FileAccessError, ModifiedFileWalkerResult> getFiles() {
        ModifiedFileVisitor modifiedFileVisitor = new ModifiedFileVisitor();

        Try<Path> tryWalk = Try.of(() -> {
            if (filePathInfo.isFollowSymlinks()) {
                EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
                return Files.walkFileTree(filePathInfo.getCurrentWorkingRootPath(), opts, Integer.MAX_VALUE, modifiedFileVisitor);
            } else {
                return Files.walkFileTree(filePathInfo.getCurrentWorkingRootPath(), modifiedFileVisitor);
            }
        });
        if (tryWalk.isSuccess()) {
            return Either.right(modifiedFileVisitor.modifiedFileWalkerResult);
        }
        return Either.left(new FileAccessError("ModifiedFileCollector: unable to walk files due to IO error"));
    }

    public enum FileModifiedComparision {
        CURRENT_IS_NEWER,
        CURRENT_IS_OLDER,
        UNCHANGED;

        /**
         * Converts each supplied {@code FileTime} to milliseconds and compares the result.
         * {@code FileTime.compareTo} cannot be used as its not guaranteed to times will be exactly the same as shown
         * below.
         *
         * <pre>
         *     current = 2011-07-19T05:54:10.783025Z
         *     backup  = 2011-07-19T05:54:10.783Z
         *
         *     current millis = 1311054850783
         *     backup millis  = 1311054850783
         * </pre>
         *
         * <p>Notice how the milliseconds are identical which is what should be used. If the times were used, current
         * is deemed more recent purely because it has 0.25ms extra time yet the millis are identical which means
         * the {@code FileTime} cannot be relied upon.</p>
         *
         * @return The {@code FileModifiedComparision} result
         */
        public static FileModifiedComparision compare(FileTime current, FileTime backup) {
            long currentMillis = current.toMillis();
            long backupMillis = backup.toMillis();

            // Difference in milliseconds must be less than threshold in milliseconds.
            boolean isUnchanged = Math.abs(currentMillis - backupMillis) <= FILE_UNCHANGED_THRESHOLD_SECONDS * 1000;

            if (isUnchanged) {
                return UNCHANGED;
            }
            if (currentMillis > backupMillis) {
                return CURRENT_IS_NEWER;
            }
            return CURRENT_IS_OLDER;
        }
    }

    private class ModifiedFileVisitor implements FileVisitor<Path> {
        // Mutated during tree walking process and must only be accessed after walking has completed.
        private ModifiedFileWalkerResult modifiedFileWalkerResult;

        public ModifiedFileVisitor() {
            this.modifiedFileWalkerResult = new ModifiedFileWalkerResult();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (dir == null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError("ModifiedFileCollector preVisitDirectory: " +
                        "Path argument is null, a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }

            if (!directoryFilter.isActive(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            }

            if (attrs == null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError(dir,"ModifiedFileCollector preVisitDirectory: " +
                        "BasicFileAttributes argument is null for " + dir.toString() + " resulting in this path being excluded from backup analysis"));
                return FileVisitResult.CONTINUE;
            }
            modifiedFileWalkerResult.incrementTotalDirectoriesScanned();

            File currentWorkingFile = dir.toFile();
            FileTime currentWorkingFileLastModified = attrs.lastModifiedTime();

            Path backupPath = filePathInfo.fromCurrentToBackupPath(dir);
            File backupFile = backupPath.toFile();

            File[] currentWorkingDirectoryFiles = currentWorkingFile.listFiles();
            boolean currentWorkingDirectoryIsEmpty = currentWorkingDirectoryFiles == null || currentWorkingDirectoryFiles.length == 0;

            /*
             * Current directory is empty and it does not exist on the backup so a new directory is marked to
             * be created on the backup. This must be handled here since the visitFile method is not invoked when the current
             * directory is empty. All other cases are handled by visitFile because even if the directory structure
             * doesn't exist, the BackupExecutor handles creating non existent parent directory hierarchies.
             */
            if (!backupFile.exists() && currentWorkingDirectoryIsEmpty) {

                // Record will create a new empty backup directory
                FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                        .currentWorkingPath(dir)
                        .backupPath(backupPath)
                        .currentLastModified(currentWorkingFileLastModified)
                        .fileSystemAction(FileSystemAction.New)
                        .fileType(toFileType(currentWorkingFile))
                        .create();

                modifiedFileWalkerResult.addFileChangeRecord(fileChangeRecord);
            }

            if (!backupFile.exists()) {
                /*
                 * Record statistics as we know a new directory will be created when the backup directory doesn't exist
                 * since its either going to be created above or when a new file is created during visitFile seems
                 * all missing parent directories will be created if need be.
                 */
                modifiedFileWalkerResult.incrementTotalNewDirectories();
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file == null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError("ModifiedFileCollector visitFile: Path argument is " +
                        "null, a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            if (attrs == null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError(file,"ModifiedFileCollector visitFile: BasicFileAttributes " +
                        "argument is null for " + file.toString() + " resulting in this path being excluded from backup analysis"));
                return FileVisitResult.CONTINUE;
            }

            modifiedFileWalkerResult.incrementTotalFilesScanned();

            File currentFile = file.toFile();
            Path backupPath = filePathInfo.fromCurrentToBackupPath(file);
            File backupFile = backupPath.toFile();

            FileTime currentLastModified = attrs.lastModifiedTime();

            if (backupFile.exists()) {
                // Compare if the current file is newer than the backup. If it is, create a file modification to
                // replace the backup version with the new current version.

                FileTime backupLastModified = FileTime.fromMillis(backupFile.lastModified());
                FileModifiedComparision modifiedComparision = FileModifiedComparision.compare(currentLastModified, backupLastModified);

                switch (modifiedComparision) {
                    case CURRENT_IS_NEWER:
                        modifiedFileWalkerResult.incrementTotalFilesModified();

                        // Mark current file to replace backup copy.
                        FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                                .currentWorkingPath(file)
                                .backupPath(backupPath)
                                .currentLastModified(currentLastModified)
                                .backupLastModified(backupLastModified)
                                .fileSystemAction(FileSystemAction.Modify)
                                .fileType(toFileType(currentFile))
                                .create();

                        modifiedFileWalkerResult.addFileChangeRecord(fileChangeRecord);
                        break;
                    case UNCHANGED:
                        modifiedFileWalkerResult.incrementTotalFilesUnmodified();
                        break;
                    case CURRENT_IS_OLDER:
                        modifiedFileWalkerResult.addFileError(new FileAccessError("ModifiedFileCollector - Current file: " + file.toString()
                                + " is older than the backup version, this path cannot be analysed for backup"));
                        break;
                }
            } else {
                // Backup file does not exist, mark the new backup file to be created.
                modifiedFileWalkerResult.incrementTotalNewFiles();

                FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                        .currentWorkingPath(file)
                        .backupPath(backupPath)
                        .currentLastModified(currentLastModified)
                        .fileSystemAction(FileSystemAction.New)
                        .fileType(toFileType(currentFile))
                        .create();

                modifiedFileWalkerResult.addFileChangeRecord(fileChangeRecord);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (file == null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError("ModifiedFileCollector - visitFileFailed: Path argument is " +
                        "null, a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            if (exc != null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError(file, exc.getMessage() + ", " + file.toString()
                        + " could not be analysed for backup."));
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (dir == null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError("ModifiedFileCollector - postVisitDirectory: Path argument is null, " +
                        "a potential path could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }

            if (exc != null) {
                modifiedFileWalkerResult.addFileError(new FileAccessError(dir, exc.getMessage() + ", " + dir.toString()
                        + " could not be analysed for backup"));
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
