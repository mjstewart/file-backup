package ui.tableCells;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A cell with containing a {@code Button} with the action to perform when triggered.
 *
 * <p>No tooltip is included as it can interfere with displaying the {@code Stage} to be opened. The {@code Stage}
 * still opens its just if the right timing of actions occurs and the tooltip can get activated and takes focus away
 * from the opening {@code Stage} which isn't nice UX.</p>
 *
 * Created by matt on 07-Jul-17.
 */
public class TableCellButton<T> extends TableCell<T, String> {

    private Consumer<T> onAction;
    private Predicate<T> isEnabled;
    private Button button;
    private HBox buttonContainer;

    /**
     * @param buttonText The {@code Button} text.
     * @param onAction The action to perform when the button is triggered.
     * @param isEnabled The predicate to determine when the button is enabled thus allowing the action {@code Consumer}
     *                  to be invoked.
     */
    public TableCellButton(String buttonText,
                           Consumer<T> onAction,
                           Predicate<T> isEnabled) {
        this.onAction = onAction;
        this.isEnabled = isEnabled;

        buttonContainer = new HBox();
        button = new Button(buttonText);
        buttonContainer.setAlignment(Pos.CENTER);
        button.getStyleClass().add("table-cell-button");
        buttonContainer.getChildren().add(button);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null && getTableRow() != null && getTableRow().getItem() != null) {
            @SuppressWarnings("unchecked")
            T record = (T) getTableRow().getItem();

            setGraphic(buttonContainer);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

            button.setDisable(!isEnabled.test(record));
            button.setOnAction(e -> {
                if (isEnabled.test(record)) {
                    onAction.accept(record);
                }
            });
         } else {
            setText(null);
            setGraphic(null);
            setTooltip(null);
        }
    }
}
