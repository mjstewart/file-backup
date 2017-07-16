package ui.tasks;

import fileBackup.fileAnalysis.FileAccessError;
import fileBackup.fileAnalysis.FileAnalysisResult;
import fileBackup.fileAnalysis.FileChangeResult;
import io.vavr.control.Either;

import java.util.Optional;

/**
 * Type to encapsulate return results from {@code FileCollectorTask}.
 *
 * Created by matt on 05-Jul-17.
 */
public class FileSystemScanResult {
    // Error if either of the results could not be computed in a CompletableFuture chain.
    private String error;

    private Either<FileAccessError, FileChangeResult> modifiedFileResult;
    private Either<FileAccessError, FileAnalysisResult> deletedFileResult;

    private FileSystemScanResult(Either<FileAccessError, FileChangeResult> modifiedFileResult,
                                Either<FileAccessError, FileAnalysisResult> deletedFileResult,
                                String error) {
        this.modifiedFileResult = modifiedFileResult;
        this.deletedFileResult = deletedFileResult;
        this.error = error;
    }

    public FileSystemScanResult(Either<FileAccessError, FileChangeResult> modifiedFileResult,
                                Either<FileAccessError, FileAnalysisResult> deletedFileResult) {
        this(modifiedFileResult, deletedFileResult, null);
    }

    public FileSystemScanResult(String error) {
        this(null, null, error);
    }

    /**
     * Contains the value only there is no value present in {@link #getError()}.
     */
    public Either<FileAccessError, FileChangeResult> getModifiedFileResult() {
        return modifiedFileResult;
    }

    /**
     * Contains the value only there is no value present in {@link #getError()}.
     */
    public Either<FileAccessError, FileAnalysisResult> getDeletedFileResult() {
        return deletedFileResult;
    }

    /**
     * An error can occur during the {@code CompletableFuture} pipeline in {@code FileCollectorTask}. If there is
     * no error then {@code getModifiedFileResult} and {@code getDeletedFileResult} will return valid results.
     *
     * @return The error.
     */
    public Optional<String> getError() {
        if (error == null) {
            return Optional.empty();
        }
        return Optional.of(error);
    }
}
