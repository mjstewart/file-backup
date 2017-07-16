package fileBackup.backupExecution;

import fileBackup.backupExecution.backupTasks.SingleBackupTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline to execute a sequence of {@code SingleBackupTask}s. A {@code BackupTaskExecutionPipeline} can consist of many
 * stages where 'stage n' depends on 'stage n - 1' being successfully executed before continuing.
 *
 * <p>Only stages that could be executed through the pipeline can be retrieved through {@link #getBackupStages()}</p>
 *
 * Created by matt on 02-Jul-17.
 */
public class BackupTaskExecutionPipeline {

    private List<BackupTaskResult> backupStages;

    private BackupTaskExecutionPipeline() {
        this.backupStages = new ArrayList<>();
    }

    /**
     * Executes the supplied {@code SingleBackupTask} as the first operation in this pipeline.
     *
     * @param task The {@code SingleBackupTask} to execute.
     * @return 'this' to allow chaining by calling {@link #andThen(SingleBackupTask)}
     */
    public static BackupTaskExecutionPipeline of(SingleBackupTask task) {
        BackupTaskExecutionPipeline backupTaskExecutionPipeline = new BackupTaskExecutionPipeline();
        backupTaskExecutionPipeline.execute(task);
        return backupTaskExecutionPipeline;
    }

    /**
     * Executes the supplied {@code SingleBackupTask} only if the status of the last {@code SingleBackupTask} is
     * {@code FileBackupStatus.SUCCESS}.
     *
     * @param task The {@code SingleBackupTask} to execute.
     * @return 'this' to allow chaining.
     */
    public BackupTaskExecutionPipeline andThen(SingleBackupTask task) {
        if (backupStages.isEmpty()) {
            return this;
        }

        BackupTaskResult lastResult = backupStages.get(backupStages.size() - 1);
        if (lastResult.getFileBackupStatus() == FileBackupStatus.SUCCESS) {
            execute(task);
        }
        return this;
    }

    private void execute(SingleBackupTask task) {
        backupStages.add(task.execute());
    }

    public List<BackupTaskResult> getBackupStages() {
        return backupStages;
    }

    /**
     * @return {@code true} if every requested {@code SingleBackupTask} resulted in {@code FileBackupStatus.SUCCESS}
     */
    public boolean allStagesSuccessful() {
        return getSuccessfulStages().size() == backupStages.size();
    }

    /**
     * @return The list of {@code BackupTaskResult} with status {@code FileBackupStatus.SUCCESS}
     */
    public List<BackupTaskResult> getSuccessfulStages() {
        return backupStages.stream()
                .filter(stage -> stage.getFileBackupStatus() == FileBackupStatus.SUCCESS)
                .collect(Collectors.toList());
    }

    /**
     * @return The list of {@code BackupTaskResult} with status {@code FileBackupStatus.FAILURE}
     */
    public List<BackupTaskResult> getFailedStages() {
        return backupStages.stream()
                .filter(stage -> stage.getFileBackupStatus() == FileBackupStatus.FAILURE)
                .collect(Collectors.toList());
    }
}
