package ui.controls;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Created by matt on 07-Jul-17.
 */
public class FormattedTableView<T> extends TableView<T> {
    /*
     * If there are FIT_TO_SIZE_LIMIT or less items, the table view height is fit to only display
     * the exact number of items.
     */
    private static final int FIT_TO_SIZE_LIMIT = 10;
    private static final int CELL_SIZE = 25;

    public FormattedTableView() {
        this(FXCollections.observableArrayList());
    }

    public FormattedTableView(ObservableList<T> items) {
        super(items);

        setOnSort(e -> e.getSource().getSelectionModel().clearSelection());

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        /*
         * adding a listener ensures all possible ways to set items result in the table being resized.
         * IMPORTANT: To ensure column headings align with column rows 1 more additional empty row needs to be added
         * to provide some extra space which is what CELL_SIZE * 2 is for.
         */
        itemsProperty().addListener(e -> {
            if (getItems().size() <= FIT_TO_SIZE_LIMIT) {
                setFixedCellSize(CELL_SIZE);
                // Add 1 cell size to account for header.
                prefHeightProperty().bind(Bindings.size(getItems()).multiply(getFixedCellSize()).add(CELL_SIZE * 2));
                refresh();
            }
        });
    }
}
