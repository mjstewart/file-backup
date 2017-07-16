package settings;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.fileAnalysis.FileValidator;
import io.vavr.control.Either;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by matt on 06-Jul-17.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public class TaskSetting {
    private String description;
    private String currentWorkingDirectory;
    private String backupDirectory;

    public TaskSetting(String description, String currentWorkingDirectory, String backupDirectory) {
        this.description = description;
        this.currentWorkingDirectory = currentWorkingDirectory;
        this.backupDirectory = backupDirectory;
    }

    public TaskSetting() {
    }

    public String getDescription() {
        return description;
    }

    public Path getCurrentWorkingDirectoryPath() {
        return Paths.get(currentWorkingDirectory);
    }

    public Path getBackupDirectoryPath() {
        return Paths.get(backupDirectory);
    }

    public Either<String, FilePathInfo> status() {
        return FilePathInfo.of(getCurrentWorkingDirectoryPath(), getBackupDirectoryPath(), new FileValidator());
    }
}
