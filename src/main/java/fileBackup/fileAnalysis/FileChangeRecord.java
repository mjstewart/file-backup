package fileBackup.fileAnalysis;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

/**
 * Represents a file needing to be changed which decides the type of concrete backup implementation to use.
 *
 * Created by matt on 30-Jun-17.
 */
public class FileChangeRecord {
    private FileType fileType;
    private FileSystemAction fileSystemAction;

    // Optional in cases where the working copy has been deleted
    private Path currentWorkingPath;

    private Path backupPath;

    // Optional as current file could be deleted if its on backup version but not on current.
    private FileTime currentLastModified;

    // Optional as when new files are created they are not on backup drive yet.
    private FileTime backupLastModified;

    private FileChangeRecord(FileType fileType, FileSystemAction fileSystemAction, Path currentWorkingPath,
                             Path backupPath, FileTime currentLastModified, FileTime backupLastModified) {
        this.fileType = fileType;
        this.fileSystemAction = fileSystemAction;
        this.currentWorkingPath = currentWorkingPath;
        this.backupPath = backupPath;
        this.currentLastModified = currentLastModified;
        this.backupLastModified = backupLastModified;
    }

    public Optional<String> getModificationTimeDifference() {
        if (fileSystemAction == FileSystemAction.Modify && backupLastModified != null && currentLastModified != null) {
           return Optional.of(TimeUtils.prettyTimeBetween(backupLastModified.toInstant(), currentLastModified.toInstant()));
        }
        return Optional.empty();
    }

    public Optional<String> getCurrentLastModifiedTimeStamp() {
        if (currentLastModified == null) return Optional.empty();
        return Optional.of(TimeUtils.format(currentLastModified.toInstant()));
    }

    public Optional<String> getBackupLastModifiedTimeStamp() {
        if (backupLastModified == null) return Optional.empty();
        return Optional.of(TimeUtils.format(backupLastModified.toInstant()));
    }

    public FileType getFileType() {
        return fileType;
    }

    public FileSystemAction getFileSystemAction() {
        return fileSystemAction;
    }

    public Optional<Path> getCurrentWorkingPath() {
        if (currentWorkingPath == null) return Optional.empty();
        return Optional.of(currentWorkingPath);
    }

    public Path getBackupPath() {
        return backupPath;
    }

    public FileTime getCurrentLastModified() {
        return currentLastModified;
    }

    public FileTime getBackupLastModified() {
        return backupLastModified;
    }

    @Override
    public String toString() {
        return "FileChangeRecord{" +
                "fileType=" + fileType +
                ", fileSystemAction=" + fileSystemAction +
                ", getCurrentWorkingPath=" + getCurrentWorkingPath() +
                ", getBackupPath=" + getBackupPath() +
                ", getCurrentLastModifiedTimeStamp=" + getCurrentLastModifiedTimeStamp() +
                ", getBackupLastModifiedTimeStamp=" + getBackupLastModifiedTimeStamp() +
                ", getModificationTimeDifference=" + getModificationTimeDifference() +
                '}';
    }

    public static class Builder {
        private FileType fileType;
        private FileSystemAction fileSystemAction;
        private Path currentWorkingPath;
        private Path backupPath;
        private FileTime currentLastModified;
        private FileTime backupLastModified;

        public Builder fileType(FileType fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder fileSystemAction(FileSystemAction fileSystemAction) {
            this.fileSystemAction = fileSystemAction;
            return this;
        }

        /**
         * Optional in cases where the working copy has been deleted
         */
        public Builder currentWorkingPath(Path currentWorkingPath) {
            this.currentWorkingPath = currentWorkingPath;
            return this;
        }

        public Builder backupPath(Path backupPath) {
            this.backupPath = backupPath;
            return this;
        }

        /**
         * Optional as current file could be deleted if its on backup version but not on current.
         */
        public Builder currentLastModified(FileTime currentLastModified) {
            this.currentLastModified = currentLastModified;
            return this;
        }

        /**
         * Optional as when new files are created they are not on backup drive yet.
         */
        public Builder backupLastModified(FileTime backupLastModified) {
            this.backupLastModified = backupLastModified;
            return this;
        }

        public FileChangeRecord create() {
            return new FileChangeRecord(fileType, fileSystemAction, currentWorkingPath, backupPath, currentLastModified, backupLastModified);
        }
    }
}
