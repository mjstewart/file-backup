package fileBackup.monitoring.persistence.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.logging.Level;

/**
 * Created by matt on 14-Jul-17.
 */
@Converter
public class LogLevelConverter implements AttributeConverter<Level, String> {
    @Override
    public String convertToDatabaseColumn(Level level) {
        return level.getName();
    }

    @Override
    public Level convertToEntityAttribute(String s) {
        return Level.parse(s);
    }
}
