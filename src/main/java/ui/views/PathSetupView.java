package ui.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import fileBackup.backupExecution.BackupTaskResult;
import fileBackup.backupExecution.FileBackupStatus;
import fileBackup.fileAnalysis.FileChangeRecord;
import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.fileAnalysis.FileValidator;
import io.vavr.control.Either;
import io.vavr.control.Try;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import settings.ApplicationSettings;
import settings.TaskSettings;
import settings.TaskSetting;
import ui.controls.StageUtils;
import ui.controls.*;
import ui.tableCells.TableCellButton;
import ui.tableCells.TableCellFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Enter current and backup path information manually or select from the dot configuration file.
 *
 * Created by matt on 04-Jul-17.
 */
public class PathSetupView extends StyledVBox {
    private Stage stage;

    private DirectoryChooser directoryChooser;

    // Feedback to understand if the directory selection is valid.
    private Label textCurrentWorkingDirectoryValidation;
    private Label textBackupDirectoryValidation;

    // Displays selected path.
    private Label textCurrentWorkingFilePath;
    private Label textBackupFilePath;

    private Button buttonManualConfigurationRun;

    private CheckBox checkBoxSymbolicLinks;

    // Populated when DirectoryChooser is invoked.
    private File backupFile;
    private File currentWorkingFile;

    // Displays user feedback to correct any errors
    private Label textFinalValidation;

    // Triggers validation once user has attempted selecting all directories.
    private int selectionAttempts = 0;

    // Passed back to caller with Either.Right containing the valid path information for the application to use.
    private Either<String, FilePathInfo> filePathInfo;

    public PathSetupView(Stage stage) {
        this.stage = stage;

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select directory");

        stage.setWidth(StageUtils.getInstance().scaleWidth(0.8));
        stage.setHeight(StageUtils.getInstance().scaleHeight(0.6));

        TitledPane autoConfigurationPane = getAutoConfigurationPane();
        TitledPane manualConfigurationPane = getManualConfigurationPane();

        autoConfigurationPane.setExpanded(false);
        manualConfigurationPane.setExpanded(false);

        getChildren().add(autoConfigurationPane);
        getChildren().add(manualConfigurationPane);

        ControlUtil.doAfterDelay(Duration.millis(500), e -> {
            autoConfigurationPane.setExpanded(true);
            manualConfigurationPane.setExpanded(true);
        });
    }

    private TitledPane getAutoConfigurationPane() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Auto configuration");

        Try<TaskSettings> backupTaskSettings =
                Try.of(ApplicationSettings::getTasksPath)
                        .flatMap(path -> Try.of(() -> {
                            if (!path.toFile().exists()) {
                                // First time application started will create all required files which user can edit later.
                                Files.createDirectories(path.getParent());
                                Files.createFile(path);
                            }
                            return new ObjectMapper().readValue(path.toFile(), TaskSettings.class);
                        }));

        if (backupTaskSettings.isFailure()) {
            titledPane.setContent(ControlUtil.getBasicErrorComponent("Unable to load configuration '" + ApplicationSettings.getTasksPath() + "' due to missing or invalid syntax"));
            return titledPane;
        }

        TableColumn<TaskSetting, String> taskDescriptionColumn = new TableColumn<>("Description");
        taskDescriptionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
        taskDescriptionColumn.setCellFactory(param -> TableCellFactory.defaultTableCell());

        TableColumn<TaskSetting, String> currentPathColumn = new TableColumn<>("Current Path");
        currentPathColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCurrentWorkingDirectoryPath().toString()));
        currentPathColumn.setCellFactory(param -> TableCellFactory.defaultTableCell());

        TableColumn<TaskSetting, String> backupPathColumn = new TableColumn<>("Backup Path");
        backupPathColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBackupDirectoryPath().toString()));
        backupPathColumn.setCellFactory(param -> TableCellFactory.defaultTableCell());

        TableColumn<TaskSetting, String> followSymLinksColumn = new TableColumn<>("Follow Sym Links");
        followSymLinksColumn.setCellValueFactory(param -> param.getValue().isFollowSymlinks() ?
                new SimpleStringProperty("Yes") : new SimpleStringProperty("No"));
        followSymLinksColumn.setCellFactory(param -> TableCellFactory.defaultTableCell());

        TableColumn<TaskSetting, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(param -> {
            Either<String, FilePathInfo> status = param.getValue().status();
            return status.isLeft() ? new SimpleStringProperty(status.getLeft()) : new SimpleStringProperty("Ready");
        });
        statusColumn.setCellFactory(param -> TableCellFactory.taskSettingStatusTableCell());

        TableColumn<TaskSetting, String> runColumn = new TableColumn<>("Action");
        runColumn.setCellValueFactory(param -> new SimpleStringProperty("Run Button"));
        runColumn.setCellFactory(param -> {
            Consumer<TaskSetting> onAction = task -> {
                // Populated field and close window as caller will come back in and access via getFilePathInfo
                filePathInfo = task.status();
                ControlUtil.fadeOutThen(this, e -> stage.close());
            };
            Predicate<TaskSetting> isEnabled = setting -> setting.status().isRight();
            return new TableCellButton<>("Run", onAction, isEnabled);
        });

        FormattedTableView<TaskSetting> tableView = new FormattedTableView<>();

        List<TaskSetting> tasks = backupTaskSettings.get().getTasks();

        // Show "Ready" settings first with failed settings last
        tasks.sort(Comparator.comparingInt(setting -> setting.status().isLeft() ? 1 : 0));

        tableView.setItems(FXCollections.observableList(tasks));

        tableView.getColumns().add(taskDescriptionColumn);
        tableView.getColumns().add(currentPathColumn);
        tableView.getColumns().add(backupPathColumn);
        tableView.getColumns().add(followSymLinksColumn);
        tableView.getColumns().add(statusColumn);
        tableView.getColumns().add(runColumn);

        titledPane.setText("Auto configuration (" + tasks.size() + ")");
        titledPane.setContent(tableView);

        return titledPane;
    }

    private TitledPane getManualConfigurationPane() {
        StyledGridPane grid = new StyledGridPane();

        textCurrentWorkingDirectoryValidation = new Label();
        textBackupDirectoryValidation = new Label();

        textCurrentWorkingFilePath = new Label();
        textCurrentWorkingFilePath.getStyleClass().add("lighter-text");
        textBackupFilePath = new Label();
        textBackupFilePath.getStyleClass().add("lighter-text");

        grid.add(new Label("Select current working directory"), 0, 0);
        Button buttonSelectCurrentWorkingDirectory = new Button("Select");
        buttonSelectCurrentWorkingDirectory.setMinWidth(70);
        grid.add(buttonSelectCurrentWorkingDirectory, 1, 0);
        grid.add(textCurrentWorkingFilePath, 0, 1);
        grid.add(textCurrentWorkingDirectoryValidation, 1, 1);

        grid.add(new Label("Select backup directory"), 0, 2);
        Button buttonSelectBackupDirectory = new Button("Select");
        buttonSelectBackupDirectory.setMinWidth(70);
        grid.add(buttonSelectBackupDirectory, 1, 2);
        grid.add(textBackupFilePath, 0, 3);
        grid.add(textBackupDirectoryValidation, 1, 3);

        checkBoxSymbolicLinks = new CheckBox("Follow symbolic links");
        checkBoxSymbolicLinks.setSelected(true);
        grid.add(checkBoxSymbolicLinks, 0, 4);

        textFinalValidation = new Label();
        textFinalValidation.getStyleClass().setAll("invalid-validation-text");
        GridPane.setMargin(textFinalValidation, new Insets(5, 0, 0, 0));

        grid.add(textFinalValidation, 0, 5);
        GridPane.setColumnSpan(textFinalValidation, GridPane.REMAINING);
        GridPane.setHalignment(textFinalValidation, HPos.CENTER);

        buttonManualConfigurationRun = new Button("Run");
        buttonManualConfigurationRun.setDisable(true);
        // Caller will come back in to this instance and get the selected filePathInfo.
        buttonManualConfigurationRun.setOnAction(e -> ControlUtil.fadeOutThen(this, event -> stage.close()));
        grid.add(buttonManualConfigurationRun, 0, 6);

        GridPane.setColumnSpan(buttonManualConfigurationRun, GridPane.REMAINING);
        GridPane.setHalignment(buttonManualConfigurationRun, HPos.CENTER);

        StyledHBox hBox = new StyledHBox(grid);
        hBox.setAlignment(Pos.CENTER);

        buttonSelectCurrentWorkingDirectory.setOnAction(e -> {
            File file = directoryChooser.showDialog(stage);
            if (file != null) {
                selectionAttempts++;
                currentWorkingFile = file;
                textCurrentWorkingFilePath.setText(file.getPath());
                validateSelectedFile();
            }
        });

        buttonSelectBackupDirectory.setOnAction(e -> {
            File file = directoryChooser.showDialog(stage);
            if (file != null) {
                selectionAttempts++;
                backupFile = file;
                textBackupFilePath.setText(file.getPath());
                validateSelectedFile();
            }
        });

        return new TitledPane("Manual configuration", hBox);
    }

    private boolean isValidDirectory(File file) {
        return file != null && file.isDirectory();
    }

    private void validateSelectedFile() {
        if (selectionAttempts >= 2 && currentWorkingFile != null && backupFile != null) {
            boolean validDirectories = true;

            if (!isValidDirectory(currentWorkingFile)) {
                applyInvalidStyle(textCurrentWorkingDirectoryValidation, "Directory does not exist");
                applyInvalidStyle(textBackupDirectoryValidation, "Invalid");
                validDirectories = false;
            }

            if (!isValidDirectory(backupFile)) {
                applyInvalidStyle(textBackupDirectoryValidation, "Directory does not exist");
                applyInvalidStyle(textCurrentWorkingDirectoryValidation, "Invalid");
                validDirectories = false;
            }

            if (!validDirectories) {
                textFinalValidation.setText("There could be issues with your file system, try rebooting");
                buttonManualConfigurationRun.setDisable(true);
            } else {
                filePathInfo = FilePathInfo.of(currentWorkingFile.toPath(), backupFile.toPath(),
                        checkBoxSymbolicLinks.isSelected(), new FileValidator());

                if (filePathInfo.isLeft()) {
                    textFinalValidation.setText(filePathInfo.getLeft());

                    applyInvalidStyle(textCurrentWorkingDirectoryValidation, "Invalid");
                    applyInvalidStyle(textBackupDirectoryValidation, "Invalid");

                    buttonManualConfigurationRun.setDisable(true);
                } else {
                    applyValidStyle(textCurrentWorkingDirectoryValidation, "Valid");
                    applyValidStyle(textBackupDirectoryValidation, "Valid");

                    textFinalValidation.setText("");
                    buttonManualConfigurationRun.setDisable(false);
                }
            }
        }
    }

    /**
     * Caller creating this UI can access the result once window is closed.
     *
     * @return The result of the user input with {@code Either.Right} containing the valid {@code FilePathInfo},
     * otherwise {@code Either.Left} can be treated as no action taking place or the validation error explaining why
     * the {@code FilePathInfo} instance could not be created.
     */
    public Either<String, FilePathInfo> getFilePathInfo() {
        if (filePathInfo == null) {
            return Either.left("No action");
        }
        return filePathInfo;
    }

    private void applyValidStyle(Label label, String text) {
        label.setText(text);
        label.getStyleClass().setAll("valid-validation-text");
    }

    private void applyInvalidStyle(Label label, String text) {
        label.setText(text);
        label.getStyleClass().setAll("invalid-validation-text");
    }
}
