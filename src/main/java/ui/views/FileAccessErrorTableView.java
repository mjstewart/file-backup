package ui.views;

import fileBackup.fileAnalysis.FileAccessError;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import ui.controls.FormattedTableView;

/**
 * Displays {@code FileAccessError}s in a {@code TableView}.
 *
 * Created by matt on 09-Jul-17.
 */
class FileAccessErrorTableView extends FormattedTableView<FileAccessError> {
    public FileAccessErrorTableView(ObservableList<FileAccessError> errors) {

        TableColumn<FileAccessError, String> errorReasonColumn = new TableColumn<>("Error Reason");
        errorReasonColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getReason()));

        TableColumn<FileAccessError, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(param -> param.getValue().getPath()
                .map(path -> new SimpleStringProperty(path.toString()))
                .orElseGet(() -> new SimpleStringProperty("")));

        setItems(errors);

        getColumns().add(errorReasonColumn);
        getColumns().add(pathColumn);
    }
}