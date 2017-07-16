package ui.controls;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * No css class is applied by default so I apply one myself without boiler plate everywhere and forgetting it.
 *
 * Created by matt on 07-Jul-17.
 */
public class StyledVBox extends VBox {
    public StyledVBox() {
        getStyleClass().add("v-box");
    }

    public StyledVBox(Node... children) {
        super(children);
        getStyleClass().add("v-box");
    }
}
