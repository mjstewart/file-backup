package ui.controls;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Created by matt on 07-Jul-17.
 */
public class ControlUtil {

    /**
     * Basic error component with centered label.
     *
     * @param message The message to display.
     * @return A {@code HBox} with the supplied message centered.
     */
    public static StyledHBox getBasicErrorComponent(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-label");
        StyledHBox primaryErrorHBox = new StyledHBox(errorLabel);
        primaryErrorHBox.setAlignment(Pos.CENTER);
        return primaryErrorHBox;
    }

    /**
     * After {@code Duration}, run the {@code EventHandler}.
     *
     * @param duration The duration.
     * @param handler The handler.
     */
    public static void doAfterDelay(Duration duration, EventHandler<ActionEvent> handler) {
        new Timeline(new KeyFrame(duration, handler)).play();
    }

    public static void fadeOutThen(Node node, EventHandler<ActionEvent> onFinished) {
        fadeThen(node, onFinished, false);
    }

    public static void fadeInThen(Node node, EventHandler<ActionEvent> onFinished) {
        fadeThen(node, onFinished, true);
    }

    private static void fadeThen(Node node, EventHandler<ActionEvent> onFinished, boolean fadeIn) {
        if (node != null && onFinished != null) {
            Timeline timeline = new Timeline();
            KeyFrame key = new KeyFrame(Duration.millis(500), new KeyValue(node.opacityProperty(), fadeIn ? 1 : 0));
            timeline.getKeyFrames().add(key);
            timeline.setOnFinished(onFinished);
            timeline.play();
        }
    }

    public static void fadeIn(Node node) {
        if (node != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(1000), node);
            ft.setFromValue(0.0);
            ft.setToValue(1);
            ft.play();
        }
    }

}
