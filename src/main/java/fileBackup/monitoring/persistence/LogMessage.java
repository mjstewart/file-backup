package fileBackup.monitoring.persistence;

import fileBackup.fileAnalysis.TimeUtils;
import fileBackup.monitoring.persistence.converters.LogLevelConverter;

import javax.persistence.*;
import java.time.Instant;
import java.util.logging.Level;

/**
 * Created by matt on 11-Jul-17.
 */
@Entity
public class LogMessage {
    @Id
    @GeneratedValue(generator = "LogMessage_SeqGen", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "LogMessage_SeqGen", sequenceName = "LogMessage_Seq", allocationSize = 1)
    private int id;

    @Column(nullable = false)
    @Convert(converter = LogLevelConverter.class)
    private Level level;

    @Column(nullable = false)
    private Instant time;

    @Column(nullable = false)
    private String message;

    public LogMessage() {}

    public LogMessage(Level level, String message) {
        this.level = level;
        this.time = Instant.now();
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public Instant getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "LogMessage{" +
                "id=" + id +
                ", level=" + level +
                ", time=" + TimeUtils.format(time) +
                ", message='" + message + '\'' +
                '}';
    }
}
