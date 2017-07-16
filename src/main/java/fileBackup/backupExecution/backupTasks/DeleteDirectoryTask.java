package fileBackup.backupExecution.backupTasks;

import fileBackup.backupExecution.BackupOperation;
import fileBackup.backupExecution.BackupTaskExecutionPipeline;
import fileBackup.backupExecution.FileWalkBackupOperation;
import fileBackup.fileAnalysis.FileAccessError;
import fileBackup.fileAnalysis.FileChangeRecord;
import fileBackup.fileAnalysis.FileSystemAction;
import fileBackup.fileAnalysis.FileType;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * {@code DeleteDirectoryTask} manages the execution of deleting all files in the directory, including the directory itself.
 *
 * <p>This is achieved through creating a {@code BackupOperation} for each file to delete and returning the corresponding
 * list of {@code BackupOperation} results.</p>
 *
 * Created by matt on 03-Jul-17.
 */
public class DeleteDirectoryTask extends BackupTask {

    private DeleteDirectoryTask(FileChangeRecord record) {
        super(record, "DeleteDirectoryTask: Delete entire backup directory " + record.getBackupPath() + " and all files within it");
    }

    public static DeleteDirectoryTask of(FileChangeRecord record) {
        return new DeleteDirectoryTask(record);
    }

    public DeleteDirectoryTaskResult execute() {
        DeleteFileVisitor deleteFileVisitor = new DeleteFileVisitor();
        Try<Path> tryDelete = Try.of(() -> Files.walkFileTree(record.getBackupPath(), deleteFileVisitor));
        if (tryDelete.isSuccess()) {
            return new DeleteDirectoryTaskResult(record, Either.right(deleteFileVisitor.fileWalkBackupOperation));
        }
        return new DeleteDirectoryTaskResult(record,
                Either.left(new BackupTaskError(this, "Unable to walk files due to IO error")));
    }

    private class DeleteFileVisitor implements FileVisitor<Path> {
        // Mutated during tree walking process and must only be accessed after walking has completed.
        private FileWalkBackupOperation fileWalkBackupOperation;

        public DeleteFileVisitor() {
            fileWalkBackupOperation = new FileWalkBackupOperation();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // Reuse DeleteFileTask, the result is then stored in BackupTaskExecutionPipeline.
            FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                    .backupPath(file)
                    .backupLastModified(attrs.lastModifiedTime())
                    .fileSystemAction(FileSystemAction.Delete)
                    .fileType(FileType.File)
                    .create();
            BackupTaskExecutionPipeline pipeline = BackupTaskExecutionPipeline.of(DeleteFileTask.of(fileChangeRecord));
            fileWalkBackupOperation.addBackupOperation((BackupOperation.of(record, pipeline)));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (exc != null) {
                String reason = "DeleteDirectoryTask: visitFileFailed, a file in this directory could not be deleted";
                fileWalkBackupOperation.addFileAccessError(new FileAccessError(file, reason));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                String reason = "DeleteDirectoryTask: postVisitDirectory failed, " + dir.toString() + " could not be deleted";
                fileWalkBackupOperation.addFileAccessError(new FileAccessError(dir, reason));
            } else {
                // Schedule directory to be deleted after all files within it have been removed.
                FileChangeRecord fileChangeRecord = new FileChangeRecord.Builder()
                        .backupPath(dir)
                        .backupLastModified(FileTime.fromMillis(dir.toFile().lastModified()))
                        .fileSystemAction(FileSystemAction.Delete)
                        .fileType(FileType.Directory)
                        .create();
                BackupTaskExecutionPipeline pipeline = BackupTaskExecutionPipeline.of(DeleteFileTask.of(fileChangeRecord));
                fileWalkBackupOperation.addBackupOperation(BackupOperation.of(record, pipeline));
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
