package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public class MainFormController {
    public JFXButton btnOpen;
    public JFXButton btnBrowse;
    public Label lblFileName;
    public Label lblFileSize;
    public Label lblDirectoryPath;
    public JFXButton btnCopy;
    public Rectangle pgbContainer;
    public Rectangle pgbProgress;
    public JFXButton btnClose;
    public Label lblProgress;
    public Label lblStatus;
    public JFXListView lstSelectedFiles;
    private File saveLocation, openedFolder;
    private List<File> openedFile;

    public void initialize() {
        /*pgbContainer.setVisible(false);
        pgbProgress.setVisible(false);
        lblProgress.setVisible(false);
        lblStatus.setVisible(false);*/
        btnCopy.setDisable(true);
    }

    public void btnOpenOnAction(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        //DirectoryChooser directoryChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Select a file to copy");
        //directoryChooser.setTitle("Select a file to copy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files(*.*)", "*.*"));
        openedFile = fileChooser.showOpenMultipleDialog(btnBrowse.getScene().getWindow());
        //openedFolder = directoryChooser.showDialog(btnBrowse.getScene().getWindow());

        if (openedFile != null) {
            lstSelectedFiles.getItems().addAll(openedFile);
        } /*else if (openedFolder != null) {
            lstSelectedFiles.getItems().add(openedFolder);
        } else{
            lblFileName.setText("No file selected");
        }*/
    }

    public void btnBrowseOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the location to be copied");
        File selectedFile = new File(System.getProperty("user.home"));
        directoryChooser.setInitialDirectory(selectedFile);
        saveLocation = directoryChooser.showDialog(btnBrowse.getScene().getWindow());

        lblDirectoryPath.setVisible(true);
        if (saveLocation != null) {
            lblDirectoryPath.setText(saveLocation + "/" + saveLocation.getName());
            btnCopy.setDisable(false);
        } else {
            lblDirectoryPath.setText("No folder selected");
        }
    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {

        if (openedFile != null) {
            for (File file : openedFile) {
                File file1 = new File(saveLocation, file.getName());
                if (!file.exists()) {
                    file1.createNewFile();
                } else {
                    Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION, "File already exists. Do you want to overwrite?", ButtonType.YES, ButtonType.NO).showAndWait();
                    if (result.get() == ButtonType.NO) return;
                }
                var task = new Task<Void>() /* class Anonymous extends Task<Void>*/ {    // <- Don't worry about this wired syntax yet
                    @Override
                    protected Void call() throws Exception {
                        FileInputStream fis = new FileInputStream(file);
                        FileOutputStream fos = new FileOutputStream(file1);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        long fileSize = file.length();
                        int totalRead = 0;
                        while (true) {
                            byte[] buffer = new byte[1024 * 10];        // 10 Kb
                            int read = bis.read(buffer);
                            totalRead += read;
                            if (read == -1) break;
                            bos.write(buffer, 0, read);
                            updateProgress(totalRead, fileSize);
                        }

                        updateProgress(fileSize, fileSize);

                        bos.close();
                        bis.close();

                        return null;
                    }
                };

                task.workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number prevWork, Number curWork) {
                        pgbProgress.setWidth(pgbContainer.getWidth() / task.getTotalWork() * curWork.doubleValue());
                        lblProgress.setText("Progress: " + formatNumber(task.getProgress() * 100) + "%");
                        lblStatus.setText(formatNumber(task.getWorkDone() / 1024.0) + " / " + formatNumber(task.getTotalWork() / 1024.0) + " kb");
                    }
                });

                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        pgbProgress.setWidth(pgbContainer.getWidth());
                        new Alert(Alert.AlertType.INFORMATION, "File has been copied successfully").showAndWait();
                        lblDirectoryPath.setText("No folder selected");
                        //lblFileName.setText("No file selected");
                        lstSelectedFiles.getItems().clear();
                        btnCopy.setDisable(true);
                        pgbProgress.setWidth(0);
                        lblProgress.setText("Progress: 0%");
                        lblProgress.setText("0 / 0 Kb");
                        // file = null;
                        // file1 = null;
                    }
                });

                new Thread(task).start();

            }
        } /*else if (openedFolder != null) {
            File folder = new File(saveLocation, openedFolder.getName());
            File[] files = openedFolder.listFiles();
            System.out.println(Arrays.toString(files));
            if (!folder.exists()) folder.mkdir();
            else{
                Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION, "Folder already exists. Do you want to overwrite?", ButtonType.YES, ButtonType.NO).showAndWait();
                if(result.get() == ButtonType.NO) return;
            }
            new Thread(()->{
                try {
                    for (File file : files) {
                        FileInputStream fis = new FileInputStream(file);
                        File file1 = new File(saveLocation+"/"+openedFolder.getName(), file.getName());
                        file1.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file1);
                        pgbContainer.setVisible(true);
                        pgbProgress.setVisible(true);
                        lblStatus.setVisible(true);
                        lblProgress.setVisible(true);
                        for (int i = 0; i < file.length(); i++) {
                            int readByte = fis.read(); // improve performance, fast
                            fos.write(readByte);
                            int k = i;
                            Platform.runLater(()->{
                                pgbProgress.setWidth(pgbContainer.getWidth()/file.length()*k);
                                lblProgress.setText("Progress: " + formatNumber((k/file.length())*100) + "%");
                                lblStatus.setText(formatNumber(k / 1024.0) + "/" + formatNumber(file.length()/1024.0) + "Kb");
                            });
                        }

                        fis.close();
                        fos.close();
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> {
                pgbProgress.setWidth(pgbContainer.getWidth());
                openedFile = null;
                saveLocation = null;
                lstSelectedFiles.getItems().clear();
                lblFileName.setText("No file selected");
                lblFileSize.setText("");
                lblDirectoryPath.setText("No folder selected");
            });
            }).start();*/
    }


    public String formatNumber(double input) {
        NumberFormat ni = NumberFormat.getNumberInstance();
        ni.setGroupingUsed(true);
        ni.setMinimumFractionDigits(1);
        ni.setMaximumFractionDigits(2);
        return ni.format(input);
    }

    public void btnCloseOnAction(ActionEvent actionEvent) {
        Platform.exit();
    }
}
