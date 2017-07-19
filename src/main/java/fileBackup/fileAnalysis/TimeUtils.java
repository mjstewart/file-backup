package fileBackup.fileAnalysis;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Created by matt on 30-Jun-17.
 */
public class TimeUtils {

    public static String prettyTimeBetween(Instant start, Instant end) {
        if (start.equals(end)) {
            return "Modified times are equal";
        }

        LocalDateTime startTime = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endTime = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        long years = ChronoUnit.YEARS.between(startTime, endTime);
        long months = ChronoUnit.MONTHS.between(startTime, endTime);
        long weeks = ChronoUnit.WEEKS.between(startTime, endTime);
        long days = ChronoUnit.DAYS.between(startTime, endTime);
        long hours = ChronoUnit.HOURS.between(startTime, endTime) % 24;
        long mins = ChronoUnit.MINUTES.between(startTime, endTime) % 60;
        long secs = ChronoUnit.SECONDS.between(startTime, endTime) % 60;

        if (years == 1) return "About " + years + " year";
        if (years > 1) return "About " + years + " years";

        if (months == 1) return "About " + months + " month";
        if (months > 1) return "About " + months + " months";

        if (weeks == 1) return "About " + weeks + " week";
        if (weeks > 1) return "About " + weeks + " weeks";

        if (days == 1) return "About " + days + " day";
        if (days > 1) return "About " + days + " days";

        if (hours == 1) return "About " + hours + " hour";
        if (hours > 1) return "About " + hours + " hours";

        StringBuilder sb = new StringBuilder();
        sb.append("About ");
        if (mins == 1) sb.append(mins).append(" min ");
        if (mins > 1) sb.append(mins).append(" mins ");
        if (secs == 1) sb.append(secs).append(" sec");
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
