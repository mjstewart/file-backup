package fileBackup.monitoring.persistence;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import settings.ApplicationSettings;

/**
 * Created by matt on 11-Jul-17.
 */
public enum HibernateUtil {
    INSTANCE;

    private final Either<String, SessionFactory> sessionFactory;

    HibernateUtil() {
        // Dynamically adds the connection url property in additional to the current hibernate.cfg.xml.
        sessionFactory = Try.of(() -> {
            String url = "jdbc:h2:" +
                    ApplicationSettings.getDatabasePath().toString();
            Configuration config = new Configuration().configure("hibernate.cfg.xml");
            config.getProperties().setProperty("hibernate.connection.url", url);
            return config.buildSessionFactory();
        }).toEither().mapLeft(t -> "Unable to create SessionFactory: " + t.getMessage());
    }

    public static Either<String, SessionFactory> getSessionFactory() {
        return INSTANCE.sessionFactory;
    }
}
