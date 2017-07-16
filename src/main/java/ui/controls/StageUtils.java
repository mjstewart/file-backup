package ui.controls;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;

/**
 * Created by matt on 07-Jul-17.
 */
public class StageUtils {
    private static StageUtils instance = new StageUtils();

    public static StageUtils getInstance() {
        return instance;
    }

    private double width;
    private double height;
    private Image logo;

    private StageUtils() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        width = bounds.getWidth() * 0.8;
        height = bounds.getHeight() * 0.8;

        logo = new Image(getClass().getResourceAsStream("/file-backup-logo.png"));
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    /**
     * @param percentage Providing 0.8 means return 80% of the current width.
     * @return The new scaled width
     */
    public double scaleWidth(double percentage) {
       return width * percentage;
    }

    /**
     * @param percentage Providing 0.8 means return 80% of the current height.
     * @return The new scaled height
     */
    public double scaleHeight(double percentage) {
        return height * percentage;
    }

    public Image getLogo() {
        return logo;
    }
}
