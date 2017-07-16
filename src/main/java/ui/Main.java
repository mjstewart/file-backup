package ui;

import fileBackup.monitoring.persistence.HibernateUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controls.ControlUtil;
import ui.controls.StageUtils;
import ui.tasks.FileBackupExecutorService;
import ui.views.HomeView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("File Backup");

        HomeView homeView = new HomeView();
        Scene scene = new Scene(homeView, StageUtils.getInstance().getWidth(), StageUtils.getInstance().getHeight());
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.getIcons().add(StageUtils.getInstance().getLogo());
        ControlUtil.fadeIn(scene.getRoot());

        primaryStage.setOnCloseRequest(e -> {
            homeView.stop();
            FileBackupExecutorService.getInstance().shutdown();
            if (HibernateUtil.getSessionFactory().isRight()) {
                HibernateUtil.getSessionFactory().get().close();
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
