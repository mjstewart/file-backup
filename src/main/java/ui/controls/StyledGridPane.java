package ui.controls;

import javafx.scene.layout.GridPane;

/**
 * No css class is applied by default so I apply one myself without boiler plate everywhere and forgetting it.
 *
 * Created by matt on 07-Jul-17.
 */
public class StyledGridPane extends GridPane {
    public StyledGridPane() {
        getStyleClass().add("grid-pane");
    }
}
