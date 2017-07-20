package ui.tableCells;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

/**
 * Custom cell highlight color based on the supplied {@code StyleRule}. The {@code StyleRule} works by calling its
 * style mapper function that accepts in an item in the table row and returns back the css to apply to this cell.
 *
 * <p>Cells get reused as per guidelines in http://docs.oracle.com/javafx/2/api/javafx/scene/control/Cell.html.</p>
 * Created by matt on 06-Jul-17.
 */
public class StyledTableCell<T> extends TableCell<T, String> {
    private StyleRule<T> styleRule;

    public StyledTableCell(StyleRule<T> styleRule) {
        getStyleClass().add("styled-cell");
        this.styleRule = styleRule;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        // TableCell could get reused so delete all possible custom styles that this cell could be in.
        styleRule.forEachStyle(cssStyle -> getStyleClass().remove(cssStyle.getCssString()));

        if (!empty && item != null && getTableRow() != null && getTableRow().getItem() != null) {
            T cellItem = (T) getTableRow().getItem();
            setText(item);

            if (!item.isEmpty()) {
                setTooltip(new Tooltip(item));
            }

            styleRule.getStyleMapper()
                    .map(mapper -> mapper.apply(cellItem))
                    .ifPresent(cssStyle -> cssStyle.ifPresent(css -> getStyleClass().add(css)));
        } else {
            setText(null);
            setTooltip(null);
        }
    }

}
