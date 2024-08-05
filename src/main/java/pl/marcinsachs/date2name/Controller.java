package pl.marcinsachs.date2name;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;


import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static pl.marcinsachs.date2name.App.*;
import static pl.marcinsachs.date2name.Operations.getAllowedFileName;

public class Controller {
    @FXML
    private ListView<String> list;

    private ObservableList<String> items = FXCollections.observableArrayList();
    public static List<File> fileList;
    public TextField datePaternTextField;
    public TextField outputDirectory;
    public Label exampleName;
    public Button selectOutputDirectory;
    public RadioButton modeCopy;
    public RadioButton modeMove;
    public RadioButton modeOverride;
    public Button okButton;
    public ProgressBar progressBar;
    public Label progressBarText;
    public String defaultDatePatern = datePatern;
    public Menu languageMenu;

    public AnchorPane root;
    public Label version;

    @FXML
    private void fileSelector(ActionEvent event) {
        Window window = ((Node) (event.getSource())).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter(ResourceBundle.getBundle("variables").getString("supportedFiles"), Operations.prepend(MediaFile.getSupportedMediaExtensions(),"*."));
        fileChooser.getExtensionFilters().add(extFilter);
        fileList = fileChooser.showOpenMultipleDialog(window);
        if (fileList != null) {
            for (File file : fileList) {
                if(!items.contains(file.getPath()))
                {
                    items.add(file.getPath());
                }


            }
            list.setItems(items);
        }
        event.consume();
    }
    @FXML
    private void directorySelector(ActionEvent event) {
        Window window = ((Node) (event.getSource())).getScene().getWindow();
        DirectoryChooser chooser = new DirectoryChooser();
        File selectedDirectory = chooser.showDialog(window);

        if (selectedDirectory != null) {

            outputDirectory.setText(selectedDirectory.getAbsolutePath());
        }
        event.consume();
    }

    @FXML
    private void onItemClick(ActionEvent event) {
        list.getItems().removeAll(
                list.getSelectionModel().getSelectedItems()
        );
    }
    @FXML
    private void languageSelector(ActionEvent event) throws IOException {
        MenuItem item = (MenuItem)event.getSource();
        String str = item.getText();

        switch (str){
            case "Polski":
                App.locale="pl";
                break;
            case "English":
                App.locale="en";
                break;
            default:
                App.locale="en";
        }
        Scene scene = root.getScene();
        scene.setRoot(FXMLLoader.load(App.class.getResource("main-view.fxml"),ResourceBundle.getBundle("variables", new Locale(locale)))); // = new Locale("en")

    }
    @FXML
    public void initialize() {

        datePaternTextField.setText(defaultDatePatern);
        outputDirectory.setText(OutputDirectory);
        switch (ModeInt){
            case 0:
                modeMove.setSelected(true);
                break;
            case 1:
                modeCopy.setSelected(true);
                break;
            case 2:
                modeOverride.setSelected(true);
                break;
            default:
                modeCopy.setSelected(true);
        }
        version.setText(appVersion);
        createDatePaternContextMenu();
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePatern);
        String date = simpleDateFormat.format(new Date());
        exampleName.setText(ResourceBundle.getBundle("variables").getString("example") + ": " + date+ ", " + date+ separator+"1"+App.postfix);
        datePaternTextField.textProperty().addListener((obs, oldText, newText)->{
            datePatern = datePaternTextField.getText();
            SimpleDateFormat simpleDateFormatNew = new SimpleDateFormat(datePatern);
            String dateNew = simpleDateFormatNew.format(new Date());
            exampleName.setText(ResourceBundle.getBundle("variables").getString("example") + ": " + dateNew+ ", " + dateNew+ separator+"1"+App.postfix);
        });
        modeOverride.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected, Boolean isNowSelected) {
                if (isNowSelected) {
                    outputDirectory.setDisable(true);
                } else {
                    outputDirectory.setDisable(false);
                }
            }
        });
    }


    @FXML

    private void onOkButtonClick(ActionEvent event) throws InterruptedException {
        if((modeOverride.isSelected()!=true) &&( outputDirectory.getText().equals("")|| outputDirectory.getText().equals(null)))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ResourceBundle.getBundle("variables").getString("emptyOutputDirectory"));
            alert.setHeaderText(ResourceBundle.getBundle("variables").getString("emptyOutputDirectoryMessage"));
            alert.show();
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            public Void call() throws Exception  {
                App.Mode mode = App.Mode.Copy;

                if(modeMove.isSelected() == true){
                    mode= App.Mode.Move;
                }
                else if(modeOverride.isSelected() == true){
                    mode= App.Mode.Override;
                }

                System.out.println(Operations.getFileList(list));
                Package appPackage = new Package(mode, Operations.getFileList(list),
                        true,outputDirectory.getText(),
                        Operations.getDateFormat(datePatern),separator);

                int numFiles = items.size();
                updateProgress(0, numFiles);


                File theDir = new File(appPackage.getOutputDirectory());
                if (!theDir.exists()){
                    theDir.mkdirs();
                }
                int i = 1;
                for(MediaFile mediaFile: appPackage.getMediaFiles()){
                    //updateProgress(i, numFiles);
                    Thread.sleep(1000);
                    double percentage = (i*100)/numFiles;

                    final double reportedProgress = percentage/100;
                    Platform.runLater(new Runnable() {
                        @SneakyThrows
                        @Override public void run() {
                            progressBar.setProgress(reportedProgress);
                            File newFile = new File(appPackage.getOutputDirectory()+"\\"+ mediaFile.getNewName()
                                    + App.postfix +"." + FilenameUtils.getExtension(mediaFile.getPath()));
                            if(appPackage.getMode() == App.Mode.Move){
                                Files.move(mediaFile.getFile().toPath(), getAllowedFileName(newFile, separator).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                            else if(appPackage.getMode() == App.Mode.Copy){

                                Files.copy(mediaFile.getFile().toPath(), getAllowedFileName(newFile, separator).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                            else if(appPackage.getMode() == App.Mode.Override){
                                newFile = new File(mediaFile.getFile().getParent()+"\\"+ mediaFile.getNewName()
                                        + App.postfix +"." + FilenameUtils.getExtension(mediaFile.getPath()));
                                mediaFile.getFile().renameTo(newFile);
                            }

                            items.remove(0);
                            list.refresh();
                            System.out.println(percentage);
                            progressBarText.setText(percentage +"%");
                        }
                    });

                    i++;
                }

                return null ;
            }
        };
        new Thread(task).start();
    }

    public void createDatePaternContextMenu(){

        EventHandler handler = new EventHandler() {
            @Override
            public void handle(Event event) {
                MenuItem item = (MenuItem)event.getSource();
                String str = item.getText().split(" ")[0].trim();
                datePaternTextField.setText(datePaternTextField.getText()+str);
            }
        };

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(defaultDatePatern);
        Date date = new Date();
        //String datestr = simpleDateFormat.format(new Date());

        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem(ResourceBundle.getBundle("variables").getString("clear"));
        MenuItem item2 = new MenuItem(ResourceBundle.getBundle("variables").getString("default"));

        //Year
        MenuItem yearLong= new MenuItem("yyyy (" + new SimpleDateFormat("yyyy").format(date)+")");
        MenuItem yearShort =  new MenuItem("yy (" + new SimpleDateFormat("yy").format(date)+")");
        Menu yearMenu = new Menu(ResourceBundle.getBundle("variables").getString("year"), null, yearLong,yearShort);
        for(MenuItem item : yearMenu.getItems()){
            item.setOnAction(handler);
        }
        //Month
        MenuItem monthFull= new MenuItem("MMMM (" + new SimpleDateFormat("MMMM").format(date)+")");
        MenuItem monthLong= new MenuItem("MMM (" + new SimpleDateFormat("MMM").format(date)+")");
        MenuItem monthShort =  new MenuItem("MM (" + new SimpleDateFormat("MM").format(date)+")");
        Menu monthMenu = new Menu(ResourceBundle.getBundle("variables").getString("month"), null,
                monthFull,
                monthLong,
                monthShort);
        for(MenuItem item : monthMenu.getItems()){
            item.setOnAction(handler);
        }

        //Day
        MenuItem dayLong= new MenuItem("dd (" + new SimpleDateFormat("dd").format(date)+")");
        MenuItem dayShort =  new MenuItem("d (" + new SimpleDateFormat("d").format(date)+")");
        Menu dayMenu = new Menu(ResourceBundle.getBundle("variables").getString("day"), null,
                dayLong,
                dayShort);
        for(MenuItem item : dayMenu.getItems()){
            item.setOnAction(handler);
        }
        //Hour
        MenuItem hourDay= new MenuItem("HH (" + new SimpleDateFormat("HH").format(date)+")");
        MenuItem hourAmPm =  new MenuItem("hh (" + new SimpleDateFormat("hh").format(date)+")");
        Menu hourMenu = new Menu(ResourceBundle.getBundle("variables").getString("hours"), null,
                hourDay,
                hourAmPm);
        for(MenuItem item : hourMenu.getItems()){
            item.setOnAction(handler);
        }

        //Minute
        MenuItem minuteLong= new MenuItem("mm (" + new SimpleDateFormat("mm").format(date)+")");
        MenuItem minuteShort =  new MenuItem("m (" + new SimpleDateFormat("m").format(date)+")");
        Menu minuteMenu = new Menu(ResourceBundle.getBundle("variables").getString("minutes"), null,
                minuteLong,
                minuteShort);
        for(MenuItem item : minuteMenu.getItems()){
            item.setOnAction(handler);
        }

        //Second
        MenuItem secondLong= new MenuItem("ss (" + new SimpleDateFormat("ss").format(date)+")");
        MenuItem secondShort =  new MenuItem("s (" + new SimpleDateFormat("s").format(date)+")");
        Menu secondMenu = new Menu(ResourceBundle.getBundle("variables").getString("seconds"), null,
                secondLong,
                secondShort);
        for(MenuItem item : secondMenu.getItems()){
            item.setOnAction(handler);
        }

        //Static Text
        MenuItem textItem1 = new MenuItem("-");
        MenuItem textItem2 = new MenuItem("_");
        MenuItem textItem3 = new MenuItem(":");
        MenuItem textItem4 = new MenuItem(".");
        MenuItem textItem5 = new MenuItem("'s'");
        MenuItem textItem6 = new MenuItem("'m'");
        MenuItem textItem7 = new MenuItem("'h'");
        MenuItem textItem8 = new MenuItem(ResourceBundle.getBundle("variables").getString("other"));
        Menu staticTextMenu = new Menu(ResourceBundle.getBundle("variables").getString("text"), null,
                textItem1,textItem2,textItem3, textItem4,textItem5,textItem6,textItem7,textItem8);
        for(MenuItem item : staticTextMenu.getItems()){
            if(item.getText()!=ResourceBundle.getBundle("variables").getString("other")){
                item.setOnAction(handler);
            }
            else{
                item.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        TextInputDialog td = new TextInputDialog();
                        td.setTitle(ResourceBundle.getBundle("variables").getString("customText"));
                        td.setHeaderText(ResourceBundle.getBundle("variables").getString("typeCustomText"));
                        td.showAndWait();
                        String str = "'" + td.getEditor().getText()+"'";
                        datePaternTextField.setText(datePaternTextField.getText()+str);
                    }
                });

            }

        }


        contextMenu.getItems().addAll(item1,item2,new SeparatorMenuItem(), yearMenu,monthMenu,dayMenu, hourMenu, minuteMenu, secondMenu,new SeparatorMenuItem(), staticTextMenu);

        item1.setOnAction((event) -> {
            datePaternTextField.setText("");
        });
        item2.setOnAction((event) -> {
            datePaternTextField.setText(defaultDatePatern);
        });


        datePaternTextField.setContextMenu(contextMenu);
    }

        @FXML
        private void savePreference(ActionEvent event){
            System.out.println("Saved!");
        int mode=1;
        if(modeMove.isSelected() == true){
            mode=0;
        } else if (modeOverride.isSelected()==true) {
            mode=2;
        }

            setPreference(locale, datePaternTextField.getText(), mode, outputDirectory.getText());

}


    public void setPreference(String lang, String datePattern, int mode, String outstr) {
        //prefs = Preferences.userNodeForPackage(App.class);

        // now set the values
        prefs.put("Lang", locale);
        prefs.put("DatePattern", datePattern);
        prefs.putInt("Mode",mode);
        prefs.put("OutputDirectory",outstr);

    }

}