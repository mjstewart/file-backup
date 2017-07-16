package ui.tableCells;

import java.util.function.Consumer;

/**
 * Created by matt on 06-Jul-17.
 */
public class CssStyleString {
    private static final String EMPTY = "";
    private String css;

    private CssStyleString(String css) {
        this.css = css;
    }

    public static CssStyleString of(String css) {
        return new CssStyleString(css);
    }

    public static CssStyleString ofEmpty() {
        return new CssStyleString(EMPTY);
    }

    public String getCssString() {
        return css;
    }

    public void ifPresent(Consumer<String> consumer) {
        if (!css.isEmpty()) {
            consumer.accept(css);
        }
    }
}
