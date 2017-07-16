package ui.tableCells;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by matt on 06-Jul-17.
 */
public class StyleRule<T> {
    private List<CssStyleString> styles;
    private Function<T, CssStyleString> styleMapper;

    private StyleRule(List<CssStyleString> styles,
                      Function<T, CssStyleString> styleMapper) {
        this.styles = styles;
        this.styleMapper = styleMapper;
    }
    private StyleRule() {
        this(null, null);
    }

    public static <T> StyleRule<T> of(List<CssStyleString> styles,
                                      Function<T, CssStyleString> styleMapper) {
        return new StyleRule<>(styles, styleMapper);
    }

    public static <T> StyleRule<T> ofEmpty() {
        return new StyleRule<>();
    }

    public Optional<List<CssStyleString>> getStyles() {
        return styles == null ? Optional.empty() : Optional.of(styles);
    }

    public Optional<Function<T, CssStyleString>> getStyleMapper() {
        return styleMapper == null ? Optional.empty() : Optional.of(styleMapper);
    }

    public void forEachStyle(Consumer<CssStyleString> consumer) {
        if (styles != null) {
            styles.forEach(consumer);
        }
    }
}
