package ui.views;

import fileBackup.backupExecution.directoryFilters.AllowAllDirectoriesFilter;
import fileBackup.backupExecution.directoryFilters.DirectoryFilter;
import fileBackup.backupExecution.directoryFilters.MonitoredDirectoryFilter;
import fileBackup.fileAnalysis.*;
import fileBackup.monitoring.DBError;
import fileBackup.monitoring.DirectoryWatcher;
import fileBackup.monitoring.persistence.LogMessage;
import fileBackup.events.Subscriber;
import fileBackup.monitoring.persistence.Repository;
import fileBackup.monitoring.persistence.WatchedFile;
import fileBackup.monitoring.persistence.FileBackupRepository;
import io.vavr.control.Either;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import settings.ApplicationSettings;
import ui.tasks.Shutdownable;
import ui.controls.*;
import ui.tableCells.TableCellFactory;
import ui.tasks.*;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The main view where all sub views are created.
 *
 * Created by matt on 04-Jul-17.
 */
public class HomeView extends StyledBorderPane implements Shutdownable {
    private final TopPanel topPanel;
    private final MenuPanel menuPanel;
    private LiveMonitoringView liveMonitoringView;

    public HomeView() {
        topPanel = new TopPanel();
        menuPanel = new MenuPanel();

        setTop(topPanel);
        setLeft(menuPanel);
    }

    /**
     * Since other views may need to access the {@code ProgressStatus}.
     *
     * @return The {@code ProgressStatus} in the top right of this view.
     */
    public ProgressStatus getProgressStatus() {
        return topPanel.progressStatus;
    }

    /**
     * Controls the disable behaviour of this {@code HomeView}.
     *
     * @param property The property to control disabled logic.
     */
    public void bindDisableProperty(BooleanExpression property) {
        menuPanel.bindDisableProperty(property);
    }

    /**
     * Sets the center view for this {@code BorderPane}.
     *
     * @param node The {@code Node} to add to the center.
     */
    public void setCenterView(Node node) {
        setCenter(node);
    }

    private void clearViews() {
        menuPanel.unbindDisableProperty();
        getProgressStatus().unbind();

        ControlUtil.fadeOutThen(getCenter(), e -> setCenter(null));
        ControlUtil.fadeOutThen(getBottom(), e -> setBottom(null));
    }

    /**
     * Executes the backup analysis and outputs results to the {@code BackupActionView}.
     *
     * <p>A {@code DirectoryFilter} is provided based on whether it is a manual backup (all directories should
     * be scanned) or if live monitoring was used in which case knowledge of modified directories can be used for
     * significant speed increases.</p>
     *
     * @param filePathInfo    The valid {@code FilePathInfo}.
     * @param directoryFilter Determines which directories should be scanned.
     */
    private void runBackupAnalysis(FilePathInfo filePathInfo, DirectoryFilter directoryFilter) {
        FileCollectorTask fileCollectorTask = new FileCollectorTask(filePathInfo, directoryFilter);
        bindDisableProperty(fileCollectorTask.runningProperty());
        topPanel.progressStatus.start("Analysing file system", fileCollectorTask.runningProperty());

        fileCollectorTask.setOnSucceeded(value ->
                setCenterView(new BackupActionView(HomeView.this, Either.right(fileCollectorTask.getValue()))));

        fileCollectorTask.setOnFailed(value ->
                setCenterView(new BackupActionView(HomeView.this,
                        Either.left(TaskFailureError.of("Analysing file system failed, please run manual backup")))));

        FileBackupExecutorService.getInstance().get().submit(fileCollectorTask);
    }

    @Override
    public void stop() {
        if (liveMonitoringView != null) {
            liveMonitoringView.stop();
        }
    }

    private class TopPanel extends StyledBorderPane {

        private ProgressStatus progressStatus;

        private TopPanel() {
            setId("home-top");

            Label title = new Label("File Backup");
            title.setId("home-title");

            progressStatus = new ProgressStatus();

            setLeft(title);
            setRight(progressStatus);
        }
    }

    private class MenuPanel extends StyledVBox {
        private Button buttonManualBackup;
        private Button buttonLiveMonitoring;


        private MenuPanel() {
            setId("home-left-menu");

            buttonManualBackup = menuButton("Manual backup");
            buttonLiveMonitoring = menuButton("Live monitoring");
            setupActions();

            getChildren().add(buttonManualBackup);
            getChildren().add(buttonLiveMonitoring);
        }

        private Button menuButton(String text) {
            Button button = new Button(text);
            button.getStyleClass().add("menu-button");
            return button;
        }

        /**
         * Disables all controls in this view.
         *
         * @param property The property to bind to.
         */
        public void bindDisableProperty(BooleanExpression property) {
            buttonManualBackup.disableProperty().bind(property);
            buttonLiveMonitoring.disableProperty().bind(property);
        }

        /**
         * Unbinds all controls in this view and re-enables back to default settings.
         */
        public void unbindDisableProperty() {
            buttonManualBackup.disableProperty().unbind();
            buttonLiveMonitoring.disableProperty().unbind();
            buttonManualBackup.setDisable(false);
            buttonLiveMonitoring.setDisable(false);
        }

        /**
         * Unbinds the disabled property of all controls which allows the controls to be manually disabled based
         * on the supplied value. You cannot set a property if it has bindings which is why this method exists.
         *
         * @param disabled {@code true} to disable otherwise {@code false}.
         */
        public void disable(boolean disabled) {
            unbindDisableProperty();
            buttonManualBackup.setDisable(disabled);
            buttonLiveMonitoring.setDisable(disabled);
        }

        private void setupActions() {
            buttonManualBackup.setOnAction(e -> {
                clearViews();
                disable(true);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(StageUtils.getInstance().getLogo());
                PathSetupView pathSetupView = new PathSetupView(stage);
                Scene scene = new Scene(pathSetupView);
                stage.setScene(scene);
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                stage.setTitle("Select paths");
                ControlUtil.fadeIn(scene.getRoot());
                stage.showAndWait();
                disable(false);

                if (pathSetupView.getFilePathInfo().isRight()) {
                    // The path information the user selected in PathSetupView is valid.
                    FilePathInfo filePathInfo = pathSetupView.getFilePathInfo().get();
                    runBackupAnalysis(filePathInfo, new AllowAllDirectoriesFilter());
                }
            });

            buttonLiveMonitoring.setOnAction(e -> {
                clearViews();
                disable(true);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.getIcons().add(StageUtils.getInstance().getLogo());
                PathSetupView pathSetupView = new PathSetupView(stage);
                Scene scene = new Scene(pathSetupView);
                stage.setScene(scene);
                scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                stage.setTitle("Select paths");
                ControlUtil.fadeIn(scene.getRoot());
                stage.showAndWait();

                if (pathSetupView.getFilePathInfo().isRight()) {
                    // The path information the user selected in PathSetupView is valid.
                    FilePathInfo filePathInfo = pathSetupView.getFilePathInfo().get();

                    GetAllTablesTask getAllTablesTask = new GetAllTablesTask();
                    bindDisableProperty(getAllTablesTask.runningProperty());
                    topPanel.progressStatus.start("Checking if previous monitoring session exists",
                            getAllTablesTask.runningProperty());

                    getAllTablesTask.setOnFailed(value ->
                            new Alert(Alert.AlertType.ERROR, "Could not run task to check if previous monitoring session exists.\n\n" +
                                    "Try restarting the application or delete " + ApplicationSettings.getDatabasePath().getParent())
                                    .showAndWait());

                    getAllTablesTask.setOnSucceeded(value -> checkPreviousMonitoringSession(filePathInfo, getAllTablesTask.getValue()));

                    FileBackupExecutorService.getInstance().get().submit(getAllTablesTask);
                } else {
                    disable(false);
                }
            });
        }

        private void checkPreviousMonitoringSession(FilePathInfo filePathInfo, AllTablesResult allTablesResult) {
            Either<DBError, FilePathInfo> eitherExistingFilePathInfo = allTablesResult.getFilePathInfo();
            Either<DBError, List<WatchedFile>> eitherAllFiles = allTablesResult.getWatchedFiles();
            Either<DBError, List<LogMessage>> eitherAllLogs = allTablesResult.getLogMessages();

            boolean existingFiles = eitherAllFiles.isRight() && !eitherAllFiles.get().isEmpty();
            boolean existingLogs = eitherAllLogs.isRight() && !eitherAllLogs.get().isEmpty();

            // If there has already been a monitoring view open then setting to null keeps it closed if cancel or close is pressed.
            liveMonitoringView = null;

            /*
             * The only time a new monitoring session won't be created is when there is a previous monitoring
             * session saved and the user presses OK in the below Alert in which case the previous session is restored.
             */
            boolean createNewMonitoringSession = true;

            /*
             * If true, db contains monitored files but if db cannot be accessed just wipe it clean and start new session.
             * A previous monitoring session can be resumed only if there is data in the db corresponding to
             * the same FilePathInfo the user just selected AND the user selects OK. All other cases clear
             * the database and create a new monitoring session.
             */
            if (eitherExistingFilePathInfo.isRight() && existingFiles && existingLogs) {

                if (eitherExistingFilePathInfo.get().equals(filePathInfo)) {
                    // Selected file info is the same as the existing file path info giving the possibility of restoring the last session.

                    String message = "Previous file changes have been detected.\n\n" +
                            "If you have not modified any files since the last file monitoring session, " +
                            "press 'OK' to resume where you left off. Otherwise press 'New' to start a new session.";

                    Optional<ButtonType> action = new Alert(Alert.AlertType.CONFIRMATION, message,
                            ButtonType.OK, new ButtonType("New"), ButtonType.CANCEL)
                            .showAndWait();

                    if (action.isPresent()) {
                        ButtonType buttonType = action.get();

                        // Nothing is needed for 'New' as it falls through to createNewMonitoringSession=true.
                        if (buttonType == ButtonType.OK) {
                            createNewMonitoringSession = false;
                            liveMonitoringView = new LiveMonitoringView(filePathInfo, eitherAllLogs.get());
                        }
                        if (buttonType == ButtonType.CANCEL) {
                            createNewMonitoringSession = false;
                        }
                    }
                }
            }

            if (createNewMonitoringSession && createNewMonitoringSession(filePathInfo)) {
                liveMonitoringView = new LiveMonitoringView(filePathInfo);
            }

            /*
             * Will be null under the following conditions.
             * 1. PathSetupView did not return a valid path
             * 2. Cancel or close X is pressed in an Alert model window
             * 3. createNewMonitoringSession returns false indicating failure
             */
            if (liveMonitoringView != null) {
                HomeView.this.setBottom(liveMonitoringView);
                ControlUtil.fadeIn(liveMonitoringView);
            } else {
                // re enable views since no action was taken in this flow.
                disable(false);
            }
        }

        /**
         * Clears database and inserts the supplied {@code FilePathInfo} ready for the new monitoring session.
         *
         * <p>Performing the clearing of the database when the {@code Path} information is set avoids needing to run
         * extra checks after the backup has been executed which simplifies things.</p>
         *
         * <p>For this method to return a successful result, the database must be completely cleared AND the new supplied
         * {@code FilePathInfo} must be saved ready for the new monitoring session. Without this guarantee the application
         * cant continue.</p>
         *
         * @param filePathInfo Corresponding to the new monitoring session.
         * @return {@code true} if successful otherwise {@code false} indicating failure.
         */
        private boolean createNewMonitoringSession(FilePathInfo filePathInfo) {
            Either<DBError, Integer> clearResult = Repository.clearDatabase();
            if (clearResult.isLeft()) {
                new Alert(Alert.AlertType.ERROR, "Database could not clear the last monitored session due to: " +
                        clearResult.getLeft().getReason() + ".\n\n" +
                        "Try restarting the application or delete " + ApplicationSettings.getDatabasePath().getParent())
                        .showAndWait();
                return false;
            }
            if (clearResult.isRight() && clearResult.get() == -1) {
                new Alert(Alert.AlertType.ERROR, "Database could not clear the last monitored session.\n\n" +
                        "Try restarting the application or delete " + ApplicationSettings.getDatabasePath().getParent())
                        .showAndWait();
                return false;
            }

            // Database is cleared, store current FilePathInfo
            Either<DBError, Serializable> saveResult = Repository.save(filePathInfo);
            if (saveResult.isLeft()) {
                new Alert(Alert.AlertType.ERROR, "Could not save current file path settings due to: " +
                        saveResult.getLeft().getReason() + ".\n\n" +
                        "Try restarting the application or try again, otherwise a manual backup can be performed.")
                        .showAndWait();
                return false;
            }
            return true;
        }
    }

    private class LiveMonitoringView extends BorderPane implements Subscriber<LogMessage>, Shutdownable {
        private MonitoringLogPane monitoringLogPane;
        private DirectoryWatcher directoryWatcher;
        private FilePathInfo filePathInfo;

        private LiveMonitoringView(FilePathInfo filePathInfo, List<LogMessage> logMessages) {
            this.filePathInfo = filePathInfo;

            double height = StageUtils.getInstance().scaleHeight(0.5);
            setMaxHeight(height);
            setPrefHeight(height);
            monitoringLogPane = new MonitoringLogPane(logMessages);

            // Try register all directories for monitoring.
            DirectoryWatcherRegistrationTask registrationTask = new DirectoryWatcherRegistrationTask(filePathInfo);
            menuPanel.bindDisableProperty(registrationTask.runningProperty());
            bindDisableProperty(registrationTask.runningProperty());
            topPanel.progressStatus.start("Setting up file system for live monitoring", registrationTask.runningProperty());

            registrationTask.setOnSucceeded(value -> {
                clearBindings();

                Either<FileAccessError, DirectoryWatcher> registrationTaskResult = registrationTask.getValue();

                if (registrationTaskResult.isLeft()) {
                    TitledPane errorPane = new TitledPane();
                    errorPane.setText("Error");
                    errorPane.setContent(ControlUtil.getBasicErrorComponent(registrationTaskResult.getLeft().getReason()));
                    errorPane.setExpanded(false);
                    ControlUtil.doAfterDelay(Duration.millis(500), e -> errorPane.setExpanded(true));
                    setCenter(errorPane);
                } else {
                    this.directoryWatcher = registrationTaskResult.get();
                    directoryWatcher.addSubscriber(this);

                    // Can't use bindings here as DirectoryWatcher isn't managed in a Task so no runningProperty is available for free.
                    menuPanel.disable(true);
                    LiveStatusActionView liveStatusActionView = new LiveStatusActionView();
                    liveStatusActionView.running(true);

                    setCenter(monitoringLogPane);
                    setBottom(liveStatusActionView);

                    /*
                     * DirectoryWatcher is not a Task because I need to control how the thread is shut down so the logs
                     * are predictable. A backup is deemed to be valid only if there are NO Level.SEVERE logs.
                     * If Task is used it just interrupts the thread with no warning which triggers SEVERE logs
                     * whereas I can control the shutdown myself and do it gracefully with no errors.
                     */
                    FileBackupExecutorService.getInstance().get().submit(this.directoryWatcher);
                }
            });

            registrationTask.setOnFailed(value -> {
                clearBindings();

                TitledPane errorPane = new TitledPane();
                errorPane.setText("Error");
                errorPane.setContent(ControlUtil.getBasicErrorComponent("Failure scheduling task to register the file system for live monitoring"));
                errorPane.setExpanded(false);
                ControlUtil.doAfterDelay(Duration.millis(500), e -> errorPane.setExpanded(true));
                setCenter(errorPane);
            });

            FileBackupExecutorService.getInstance().get().submit(registrationTask);
        }

        private LiveMonitoringView(FilePathInfo filePathInfo) {
            this(filePathInfo, null);
        }

        /**
         * There may be previous bindings to some controls but we no longer want them since some controls need to
         * be manually changed. You cannot set values manually if there are bindings without exceptions being thrown.
         */
        private void clearBindings() {
            menuPanel.unbindDisableProperty();
            topPanel.progressStatus.unbind();
        }

        @Override
        public void update(LogMessage item) {
            // No Task is used to run DirectoryWatcher meaning log messages will be sent from another thread.
            Platform.runLater(() -> monitoringLogPane.observableLogMessages.add(item));
        }

        @Override
        public void stop() {
            if (directoryWatcher != null) {
                directoryWatcher.stop();
            }
        }

        private class MonitoringLogPane extends TitledPane {
            private ObservableList<LogMessage> observableLogMessages;

            private MonitoringLogPane(List<LogMessage> logMessages) {
                FormattedTableView<LogMessage> tableView = new FormattedTableView<>();
                tableView.setPlaceholder(new Label("No files have changed yet"));

                tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

                TableColumn<LogMessage, String> statusColumn = new TableColumn<>("Time");
                statusColumn.setCellValueFactory(param -> new SimpleStringProperty(TimeUtils.logFormat(param.getValue().getTime())));
                statusColumn.setCellFactory(param -> TableCellFactory.logMessageTableCell());
                statusColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));

                TableColumn<LogMessage, String> messageColumn = new TableColumn<>("Message");
                messageColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMessage()));
                messageColumn.setCellFactory(param -> TableCellFactory.defaultTableCell());
                messageColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.8));

                tableView.getColumns().add(statusColumn);
                tableView.getColumns().add(messageColumn);

                observableLogMessages = logMessages == null ? FXCollections.observableArrayList()
                        : FXCollections.observableArrayList(logMessages);

                SortedList<LogMessage> sortedLogMessages = new SortedList<>(observableLogMessages,
                        Comparator.comparing(LogMessage::getTime).reversed());

                tableView.setItems(sortedLogMessages);
                textProperty().bind(Bindings.concat("Live monitoring activity (", Bindings.size(tableView.getItems()), ")"));
                setContent(tableView);
                setExpanded(false);
                ControlUtil.doAfterDelay(Duration.millis(500), e -> setExpanded(true));
            }
        }

        private class LiveStatusActionView extends BorderPane {
            private Label statusLabel;
            private Button buttonRunBackup;

            private LiveStatusActionView() {
                statusLabel = new Label();
                statusLabel.getStyleClass().add("monitoring-status-label");
                setId("live-status-action-view");
                setRight(statusLabel);

                buttonRunBackup = new Button("Stop and run backup");
                buttonRunBackup.getStyleClass().add("monitoring-button");
                setLeft(buttonRunBackup);

                buttonRunBackup.setOnAction(e -> {
                    directoryWatcher.stop();
                    running(false);
                    clearViews();

                    Either<DBError, List<LogMessage>> logMessagesEither = FileBackupRepository.getAllLogMessages();

                    if (logMessagesEither.isLeft()) {
                        String reason = logMessagesEither.getLeft().getReason();
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to analyse file activity: " + reason + ". " +
                                "Please run a manual backup or try restarting the application.");
                        alert.showAndWait();
                        return;
                    }

                    // By convention, severe level logs indicate some directories had issues being watched.
                    boolean hasErrors = logMessagesEither.get().stream()
                            .filter(logMessage -> logMessage.getLevel().equals(Level.SEVERE))
                            .count() > 0;

                    if (hasErrors) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "There have been errors detected " +
                                "that could result in loss of data, do you want to continue with the backup?\n\n" +
                                "Selecting cancel gives you the option of running a manual backup.");
                        alert.showAndWait()
                                .filter(response -> response == ButtonType.OK)
                                .ifPresent(buttonType -> processBackup());
                    } else {
                        processBackup();
                    }
                });
            }

            /**
             * Gets all {@code WatchedFile}s stored in the database and creates a {@code Set} of hashCodes
             * which act as the lookup value for determining if a directory can be skipped.
             *
             * <p><b>The overall process</b></p>
             *
             * <p>The {@code MonitoredDirectoryFilter} contains the {@code Set} of hashCodes of each {@code WatchedFile}
             * in the database achieved through a {@code DirectoryFilter}. When an {@code AbstractFileCollector} does the
             * file walk and enters the {@code preVisitDirectory} method, the path is converted to its corresponding
             * hashCode by using {@code PathMappingStrategy.getUnmappedHashCode} and checked to see if its in the {@code Set}.
             * If the {@code DirectoryFilter} says this path is active, the directory has seen activity and must be
             * scanned to collect the modified files, otherwise the entire directory can be skipped.</p>
             */
            private void processBackup() {
                Either<DBError, List<WatchedFile>> watchedFilesEither = FileBackupRepository.getAllFiles();

                if (watchedFilesEither.isLeft()) {
                    String reason = watchedFilesEither.getLeft().getReason();
                    new Alert(Alert.AlertType.ERROR, "Unable to access analysed files to perform backup: " + reason +
                            ". Please run a manual backup or try restarting the application.")
                            .showAndWait();
                } else {
                    Set<Integer> activeDirectoryHashCodes = watchedFilesEither.get().stream()
                            .map(WatchedFile::getHashCode)
                            .collect(Collectors.toSet());

                    runBackupAnalysis(filePathInfo, new MonitoredDirectoryFilter(activeDirectoryHashCodes, filePathInfo));
                }
            }

            /**
             * When the supplied value is true, apply the behaviour of setting the status label to live and enabling
             * the run backup button.
             *
             * @param value {@code true} if monitoring is live, otherwise {@code false}.
             */
            private void running(boolean value) {
                statusLabel.getStyleClass().remove("monitoring-status-label-active");
                statusLabel.getStyleClass().remove("monitoring-status-label-inactive");
                if (value) {
                    statusLabel.getStyleClass().add("monitoring-status-label-active");
                    statusLabel.setText("Live");
                } else {
                    statusLabel.getStyleClass().add("monitoring-status-label-inactive");
                    statusLabel.setText("Stopped");
                }
                buttonRunBackup.setDisable(!value);
            }
        }
    }
}