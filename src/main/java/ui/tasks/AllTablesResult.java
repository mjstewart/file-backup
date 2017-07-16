package ui.tasks;

import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.monitoring.DBError;
import fileBackup.monitoring.persistence.LogMessage;
import fileBackup.monitoring.persistence.WatchedFile;
import io.vavr.control.Either;

import java.util.List;

/**
 * Stores the result of querying the database for each table needed to analyse if there is a previous monitoring
 * session saved.
 *
 * Created by matt on 16-Jul-17.
 */
public class AllTablesResult {
    private Either<DBError, FilePathInfo> filePathInfo;
    private Either<DBError, List<WatchedFile>> watchedFiles;
    private Either<DBError, List<LogMessage>> logMessages;

    public AllTablesResult(Either<DBError, FilePathInfo> filePathInfo,
                           Either<DBError, List<WatchedFile>> watchedFiles,
                           Either<DBError, List<LogMessage>> logMessages) {
        this.filePathInfo = filePathInfo;
        this.watchedFiles = watchedFiles;
        this.logMessages = logMessages;
    }

    public Either<DBError, FilePathInfo> getFilePathInfo() {
        return filePathInfo;
    }

    public Either<DBError, List<WatchedFile>> getWatchedFiles() {
        return watchedFiles;
    }

    public Either<DBError, List<LogMessage>> getLogMessages() {
        return logMessages;
    }
}
