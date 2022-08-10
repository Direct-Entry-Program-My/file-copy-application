package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.text.NumberFormat;
import java.util.Arrays;
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

    private File saveLocation, openedFolder;
    private List<File> openedFile;
    public Label lblProgress;
    public Label lblStatus;
    public JFXListView lstSelectedFiles;

    public void initialize(){
        pgbContainer.setVisible(false);
        pgbProgress.setVisible(false);
        btnCopy.setDisable(true);
        lblProgress.setVisible(false);
        lblStatus.setVisible(false);
    }

    public void btnOpenOnAction(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        //DirectoryChooser directoryChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Select a file to copy");
        //directoryChooser.setTitle("Select a file to copy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files(*.*)","*.*"));
        openedFile = fileChooser.showOpenMultipleDialog(btnBrowse.getScene().getWindow());
        //openedFolder = directoryChooser.showDialog(btnBrowse.getScene().getWindow());

        if(openedFile != null){
            lstSelectedFiles.getItems().addAll(openedFile);
        } /*else if (openedFolder != null) {
            lstSelectedFiles.getItems().add(openedFolder);
        }*/ else{
            lblFileName.setText("No file selected");
        }
    }

    public void btnBrowseOnAction(ActionEvent actionEvent) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the location to be copied");
        File selectedFile = new File(System.getProperty("user.home"));
        directoryChooser.setInitialDirectory(selectedFile);
        saveLocation = directoryChooser.showDialog(btnBrowse.getScene().getWindow());

        lblDirectoryPath.setVisible(true);
        if(saveLocation != null){
            lblDirectoryPath.setText(saveLocation.getName());
            btnCopy.setDisable(false);
        }else {
            lblDirectoryPath.setText("No folder selected");
        }
    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {

        if (openedFile != null) {
            for (File file : openedFile) {
                File file1 = new File(saveLocation, file.getName());
                if(!file.exists()){
                    file1.createNewFile();
                }else{
                    Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION, "File already exists. Do you want to overwrite?", ButtonType.YES, ButtonType.NO).showAndWait();
                    if(result.get() == ButtonType.NO) return;
                }
                new Thread(()->{
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        FileOutputStream fos = new FileOutputStream(file1);
                        pgbProgress.setVisible(true);
                        pgbContainer.setVisible(true);
                        lblStatus.setVisible(true);
                        lblProgress.setVisible(true);
                        for (int i = 0; i < file.length(); i++) {
                            int readByte = fis.read();
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
                }).start();

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



    public String formatNumber(double input){
        NumberFormat ni = NumberFormat.getNumberInstance();
        ni.setGroupingUsed(true);
        ni.setMinimumFractionDigits(2);
        ni.setMaximumFractionDigits(2);
        return ni.format(input);
    }
    public void btnCloseOnAction(ActionEvent actionEvent) {
        Platform.exit();
    }
}
