package ui.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application wide {@code ExecutorService}.
 *
 * Created by matt on 05-Jul-17.
 */
public class FileBackupExecutorService {
    private static FileBackupExecutorService instance = new FileBackupExecutorService();

    private final ExecutorService executorService;

    public static FileBackupExecutorService getInstance() {
        return instance;
    }

    private FileBackupExecutorService() {
        executorService = Executors.newCachedThreadPool();
    }

    public ExecutorService get() {
        return executorService;
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}
