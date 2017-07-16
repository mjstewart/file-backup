package ui.views;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import ui.controls.StyledHBox;


/**
 * Contains a {@code ProgressIndicator} and associated label.
 *
 * Created by matt on 05-Jul-17.
 */
public class ProgressStatus extends StyledHBox {
    private Label status;
    private ProgressIndicator progressIndicator;

    public ProgressStatus() {
        status = new Label();
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        setSpacing(10);
        getChildren().addAll(progressIndicator, status);
    }

    /**
     * Begin running the progress indicator.
     *
     * @param whenRunningMessage The text to display when the indicator is displayed.
     * @param property The property that controls when the status text and progress indicator are visible.
     */
    public void start(String whenRunningMessage, BooleanExpression property) {
        progressIndicator.visibleProperty().bind(property);

        status.textProperty().bind(
                Bindings.when(property)
                        .then(Bindings.concat(whenRunningMessage, "..."))
                        .otherwise(""));
        status.visibleProperty().bind(property);
    }

    public void unbind() {
        progressIndicator.visibleProperty().unbind();
        status.visibleProperty().unbind();
    }
}
