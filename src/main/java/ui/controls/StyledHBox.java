package ui.controls;

import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * No css class is applied by default so I apply one myself without boiler plate everywhere and forgetting it.
 *
 * Created by matt on 07-Jul-17.
 */
public class StyledHBox extends HBox {
    public StyledHBox() {
        getStyleClass().add("h-box");
    }

    public StyledHBox(Node... children) {
        super(children);
        getStyleClass().add("h-box");
    }
}
