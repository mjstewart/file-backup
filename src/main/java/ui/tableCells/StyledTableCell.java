package ui.tableCells;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

/**
 * Custom cell highlight color, plus allows a table row to be selected by showing the full row highlight rather than
 * cells with a custom renderer blocking the row highlight making it look ugly.
 *
 * <p>For example, when a row is selected, all css is removed allowing the row highlight to span the entire row which
 * looks nice. When the row is unselected the css for the table cell gets reapplied.</p>
 *
 * Created by matt on 06-Jul-17.
 */
public class StyledTableCell<T> extends TableCell<T, String> {
    // Toggles cell styling when row is selected.
    private boolean selected = false;

    private boolean listenerApplied = false;
    private StyleRule<T> styleRule;

    /*
     * A cell can be reused and its value changes upon sorting. The 'last' model object this cell was displaying
     * is stored to allow the cells previous style to be removed in selectedIndexProperty listener.
     *
     * To motivate the problem, if cellItem was a local variable within updateItem, the cell displays incorrect style
     * because the new value in getTableRow().getItem() upon sorted could be a new model object with a different
     * style but since the reordering of the table has occurred, there is no way to get the old object back to remove
     * its style.
     */
    private T cellItem;

    public StyledTableCell(StyleRule<T> styleRule) {
        this.styleRule = styleRule;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        // TableCell could get reused so delete all possible custom styles that this cell could be in.
        styleRule.forEachStyle(cssStyle -> getStyleClass().remove(cssStyle.getCssString()));

        /*
         * Listener only needs to be put on a cell once then it reacts to the changed 'cellItem' over the cells lifetime.
         * The purpose of this listener is to react to row selections by removing the css of the this cell to allow
         * the entire row selection color to be applied rather than be blocked out any custom style applied which
         * looks ugly.
         */
        if (!listenerApplied) {
            getTableView().getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                if (cellItem != null) {
                    // This cell is selected, remove style so row highlighting applies to entire row.
                    if (newValue.intValue() == getIndex() && !selected) {
                        selected = true;
                        styleRule.getStyleMapper()
                                .map(mapper -> mapper.apply(cellItem))
                                .ifPresent(cssStyle -> cssStyle.ifPresent(css -> getStyleClass().remove(css)));
                    }
                    if (newValue.intValue() != getIndex() && selected) {
                        // New cell selected and this cell was previously selected, reapply style.
                        selected = false;
                        styleRule.getStyleMapper()
                                .map(mapper -> mapper.apply(cellItem))
                                .ifPresent(cssStyle -> cssStyle.ifPresent(css -> getStyleClass().add(css)));
                    }
                }
            });
            listenerApplied = true;
        }

        if (!empty && item != null && getTableRow() != null && getTableRow().getItem() != null) {
            cellItem = (T) getTableRow().getItem();

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
