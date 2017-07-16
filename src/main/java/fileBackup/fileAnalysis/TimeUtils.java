package fileBackup.fileAnalysis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by matt on 30-Jun-17.
 */
public class TimeUtils {

    public static String prettyTimeBetween(Instant start, Instant end) {
        long hours = ChronoUnit.HOURS.between(start, end) % 24;
        long mins = ChronoUnit.MINUTES.between(start, end) % 60;
        long secs = ChronoUnit.SECONDS.between(start, end) % 60;

        if (hours == 1) return "About " + hours + " hour";
        if (hours > 1) return "About " + hours + " hours";

        StringBuilder sb = new StringBuilder();
        sb.append("About ");
        if (mins == 1) sb.append(mins).append("min ");
        if (mins > 1) sb.append(mins).append(" mins ");
        if (secs == 1) sb.append("1 sec");
        if (secs > 1) sb.append(secs).append(" secs");

        return sb.toString();
    }

    public static String format(Instant instant) {
        return format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    public static String logFormat(Instant instant) {
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return time.format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss:SSS a"));
    }

    public static String format(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss a"));
    }
}
