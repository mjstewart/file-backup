package fileBackup.monitoring.persistence;

import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.monitoring.DBError;
import io.vavr.control.Either;
import org.hibernate.Session;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by matt on 11-Jul-17.
 */
public class FileBackupRepository {

    /**
     * Tries to find the {@code WatchedFile} by its hashCode.
     *
     * <p><b>Usage:</b> To check if no result was found</p>
     * <pre>
     *     Either<DBError, WatchedFile> getFile = watchedFileRepository.getFile(...)
     *
     *     if (getFile.isLeft()) {
     *        DBError error = getFile.getLeft();
     *        if (error.getThrowable() instanceof NoResultException) {
     *            // handle no result
     *        } else {
     *            // error.getReason() will contain the database access error
     *        }
     *    }
     * </pre>
     *
     * @return If Either.left there is either no record which is determined in the above usage guide or there has
     * been issues accessing the database. Otherwise Either.right contains the existing {@code WatchFile} for this parent path.
     */
    public static Either<DBError, WatchedFile> getFile(int hashCode) {
        Function<Session, Supplier<WatchedFile>> mapper = openSession -> {
            String hql = "from WatchedFile where hashCode = :hashCode";
            return () -> openSession.createQuery(hql, WatchedFile.class)
                    .setParameter("hashCode", hashCode)
                    .setMaxResults(1)
                    .getSingleResult();
        };
        return Repository.runQuery(mapper);
    }

    public static Either<DBError, List<WatchedFile>> getFiles(List<Integer> hashCodes) {
        Function<Session, Supplier<List<WatchedFile>>> mapper = openSession -> {
            String hql = "from WatchedFile where hashCode in :hashCodes";
            return () -> openSession.createQuery(hql, WatchedFile.class)
                    .setParameter("hashCodes", hashCodes)
                    .list();
        };
        return Repository.runQuery(mapper);
    }

    public static Either<DBError, List<WatchedFile>> getAllFiles() {
        Function<Session, Supplier<List<WatchedFile>>> mapper = openSession ->
                () -> openSession.createQuery("from WatchedFile", WatchedFile.class).list();
        return Repository.runQuery(mapper);
    }

    public static Either<DBError, List<LogMessage>> getAllLogMessages() {
        Function<Session, Supplier<List<LogMessage>>> mapper = openSession ->
                () -> openSession.createQuery("from LogMessage", LogMessage.class).list();
        return Repository.runQuery(mapper);
    }

    /**
     * Saves all {@code WatchedFile}s in the collection.
     *
     * @param files The elements to save.
     * @return {@code true} if all values were saved, otherwise {@code false} if there was at least 1 error.
     */
    public static boolean saveAll(Collection<WatchedFile> files) {
        return files != null && files.stream()
                .map(Repository::save)
                .filter(Either::isLeft)
                .count() == 0;
    }

    /**
     * Only a single {@code FilePathInfo} is needed as it contains the current working directory and backup directory
     * that all active directories are for. If the application starts back up again and the database has 1
     * {@code FilePathInfo} and at least 1 {@code WatchedFile} the monitoring session can be restored using this
     * {@code FilePathInfo} for the next session to continue where it left off.
     *
     * @return The single {@code FilePathInfo} that is for all entries in the database.
     */
    public static Either<DBError, FilePathInfo> getFilePathInfo() {
        Function<Session, Supplier<FilePathInfo>> mapper = openSession ->
                () -> openSession.createQuery("from FilePathInfo", FilePathInfo.class).getSingleResult();
        return Repository.runQuery(mapper);
    }
}
