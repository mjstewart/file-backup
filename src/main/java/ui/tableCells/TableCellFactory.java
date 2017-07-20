package ui.tableCells;

import fileBackup.backupExecution.BackupOperation;
import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.backupExecution.backupTasks.DeleteDirectoryTaskResult;
import fileBackup.fileAnalysis.FileChangeRecord;
import fileBackup.monitoring.persistence.LogMessage;
import settings.TaskSetting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * A factory to create styled {@code TableCell}s used throughout the applications {@code TableView}s.
 *
 * Created by matt on 06-Jul-17.
 */
public class TableCellFactory {

    public static <T> StyledTableCell<T> defaultTableCell() {
        return new StyledTableCell<>(StyleRule.ofEmpty());
    }

    public static StyledTableCell<FileChangeRecord> fileSystemActionTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("file-system-action-new"),
                CssStyleString.of("file-system-action-modify"),
                CssStyleString.of("file-system-action-delete"));

        Function<FileChangeRecord, CssStyleString> styleMapper = record -> {
            switch (record.getFileSystemAction()) {
                case New:
                    return styles.get(0);
                case Modify:
                    return styles.get(1);
                case Delete:
                    return styles.get(2);
                default:
                    return CssStyleString.ofEmpty();
            }
        };
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<FileChangeRecord> currentPathTableCell() {
        List<CssStyleString> styles = Arrays.asList(CssStyleString.of("missing-cell-value"));

        Function<FileChangeRecord, CssStyleString> styleMapper = record -> {
            if (!record.getCurrentWorkingPath().isPresent()) {
                return styles.get(0);
            }
            return CssStyleString.ofEmpty();
        };
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<TaskSetting> taskSettingStatusTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("task-setting-ready"),
                CssStyleString.of("task-setting-error"));

        Function<TaskSetting, CssStyleString> styleMapper =
                record -> record.status().isLeft() ? styles.get(1) : styles.get(0);

        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<BackupOperation> backupOperationExecutionStatusTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("backup-execution-success"),
                CssStyleString.of("backup-execution-failure"));

        Function<BackupOperation, CssStyleString> styleMapper = operation -> {
            switch (operation.getFileBackupStatus()) {
                case SUCCESS:
                    return styles.get(0);
                case FAILURE:
                    return styles.get(1);
                default:
                    return CssStyleString.ofEmpty();
            }
        };
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<BackupOperation> backupOperationFileSystemActionTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("backup-execution-file-system-action-new"),
                CssStyleString.of("backup-execution-file-system-action-modify"),
                CssStyleString.of("backup-execution-file-system-action-delete"));

        Function<BackupOperation, CssStyleString> styleMapper = operation -> {
            switch (operation.getFileChangeRecord().getFileSystemAction()) {
                case New:
                    return styles.get(0);
                case Modify:
                    return styles.get(1);
                case Delete:
                    return styles.get(2);
                default:
                    return CssStyleString.ofEmpty();
            }
        };
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<BackupTaskResult> backupTaskResultFileBackupStatusTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("backup-execution-success"),
                CssStyleString.of("backup-execution-failure"));

        Function<BackupTaskResult, CssStyleString> styleMapper = taskResult -> {
            switch (taskResult.getFileBackupStatus()) {
                case SUCCESS:
                    return styles.get(0);
                case FAILURE:
                    return styles.get(1);
                default:
                    return CssStyleString.ofEmpty();
            }
        };
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<DeleteDirectoryTaskResult> deleteDirectoryTaskResultTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("backup-execution-success"),
                CssStyleString.of("backup-execution-failure"));

        Function<DeleteDirectoryTaskResult, CssStyleString> styleMapper = taskResult ->
                taskResult.isValid() ? styles.get(0) : styles.get(1);
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }

    public static StyledTableCell<LogMessage> logMessageTableCell() {
        List<CssStyleString> styles = Arrays.asList(
                CssStyleString.of("log-message-severe"),
                CssStyleString.of("log-message-warning"),
                CssStyleString.of("log-message-info"));

        Function<LogMessage, CssStyleString> styleMapper = logMessage -> {
            Level level = logMessage.getLevel();
            if (level.equals(Level.SEVERE)) return styles.get(0);
            if (level.equals(Level.WARNING)) return styles.get(1);
            if (level.equals(Level.INFO)) return styles.get(2);
            return CssStyleString.ofEmpty();
        };
        return new StyledTableCell<>(StyleRule.of(styles, styleMapper));
    }
}
