package fileBackup.monitoring.persistence;

import fileBackup.backupExecution.BackupOperation;
import fileBackup.backupExecution.backupTasks.BackupTaskError;
import fileBackup.backupExecution.backupTasks.DeleteDirectoryTask;
import fileBackup.backupExecution.backupTasks.DeleteDirectoryTaskResult;
import fileBackup.fileAnalysis.FileAccessError;
import fileBackup.fileAnalysis.FileChangeRecord;
import fileBackup.fileAnalysis.FileSystemAction;
import fileBackup.fileAnalysis.FileType;
import fileBackup.monitoring.DBError;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import settings.ApplicationSettings;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by matt on 12-Jul-17.
 */
public interface Repository {
    /**
     * Reduces code duplication by opening and closing a {@code Session} in one place. The key to understanding
     * why this works is the query mapper returns a {@code Supplier} which is lazy. The calling code must not
     * invoke the query otherwise each method needs to wrap it up in try/catch blocks and do the same boilerplate.
     * By making it lazy, this function passes the open {@code Session} in and gets back the lazy query to then
     * invoke which either produces a result or causes an error which is followed by the {@code Session} being closed.
     *
     * @param queryMapper Accepts an open {@code Session} and returns the action to be applied on this {@code Session}.
     * @param <T>         The type returned as a result of invoking the {@code Supplier}.
     * @return Either an error or the successful result.
     */
    static <T> Either<DBError, T> runQuery(Function<Session, Supplier<T>> queryMapper) {
        if (HibernateUtil.getSessionFactory().isRight()) {
            return Try.of(() -> {
                SessionFactory sessionFactory = HibernateUtil.getSessionFactory().get();
                Session session = sessionFactory.openSession();
                T result = queryMapper.apply(session).get();
                session.close();
                return result;
            }).toEither().mapLeft(DBError::of);
        }
        return Either.left(DBError.of(new Throwable("No session factory available")));
    }

    /**
     * Runs the {@code Supplier} returned by the provided {@code mapper} which takes in an open {@code Session} and
     * performs some action within a transaction.
     * <p>
     * <p>The mapper works the same way as {@link #runQuery}</p>
     *
     * @param mapper Accepts an open {@code Session} and returns the action to be applied on this {@code Session}.
     * @param <T>    The type returned as a result of invoking the {@code Supplier}.
     * @return Either an error or the successful result.
     */
    static <T> Either<DBError, T> runTransaction(Function<Session, Supplier<T>> mapper) {
        if (HibernateUtil.getSessionFactory().isRight()) {
            Transaction tx = null;
            Session session = null;
            try {
                SessionFactory sessionFactory = HibernateUtil.getSessionFactory().get();
                session = sessionFactory.openSession();
                tx = session.beginTransaction();
                T result = mapper.apply(session).get();
                tx.commit();
                return Either.right(result);
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                return Either.left(DBError.of(e));
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
        return Either.left(DBError.of(new Throwable("No session factory available")));
    }

    /**
     * @param entity The entity to save.
     * @param <T>    The type of the entity.
     * @return The error or the primary key.
     */
    static <T> Either<DBError, Serializable> save(T entity) {
        return runTransaction(openSession -> () -> openSession.save(entity));
    }

    static <T> Either<DBError, Void> update(T entity) {
        return runTransaction(openSession -> () -> {
            openSession.update(entity);
            return null;
        });
    }

    static <T> Either<DBError, Void> saveOrUpdate(T entity) {
        return runTransaction(openSession -> () -> {
            openSession.saveOrUpdate(entity);
            return null;
        });
    }

    /**
     * Clears all tables and resets auto increment sequences back to 1 to avoid needing to worry about sequences
     * overflowing if this application is used over many years...
     *
     * <p>H2 truncate table does NOT reset primary key auto increment values (it may be implemented in future versions.
     * The db directory in .filebackup application settings could easily just be deleted to reset the tables but this
     * relies on the user needing to do it.</p>
     *
     * <p>The executeUpdate statement returns -1 if there are problems. reduceRight goes through all steps that
     * completed and keeps -1 if there are issues indicating an overall failure.</p>
     *
     * @return The error of the first transaction to fail otherwise 0 if all table modifications were successful.
     * If -1 is returned, there was a problem executing one of the table modifications.
     */
    static Either<DBError, Integer> clearDatabase() {
        return new SequencedTransaction<Integer>()
                .start(openSession -> () -> openSession.createNativeQuery("TRUNCATE TABLE WatchedFile").executeUpdate())
                .andThen(openSession -> () -> openSession.createNativeQuery("TRUNCATE TABLE LogMessage").executeUpdate())
                .andThen(openSession -> () -> openSession.createNativeQuery("TRUNCATE TABLE FilePathInfo").executeUpdate())
                .andThen(openSession -> () -> openSession.createNativeQuery("ALTER SEQUENCE WatchedFile_Seq RESTART WITH 1").executeUpdate())
                .andThen(openSession -> () -> openSession.createNativeQuery("ALTER SEQUENCE LogMessage_Seq RESTART WITH 1").executeUpdate())
                .andThen(openSession -> () -> openSession.createNativeQuery("ALTER SEQUENCE FilePathInfo_Seq RESTART WITH 1").executeUpdate())
                .reduceRight((a, b) -> (a == -1 || b == -1) ? -1 : 0)
                .orElse(Either.left(DBError.of(new Throwable("Unable to clear the database"))));
    }
}
