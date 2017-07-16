package fileBackup.monitoring;

/**
 * Created by matt on 11-Jul-17.
 */
public class DBError {
    private Throwable throwable;

    private DBError(Throwable throwable) {
        this.throwable = throwable;
    }

    public static DBError of(Throwable throwable) {
        return new DBError(throwable);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getReason() {
        return throwable.getMessage();
    }

    @Override
    public String toString() {
        return "DBError=" + getReason();
    }
}
