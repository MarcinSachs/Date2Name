package pl.marcinsachs.date2name;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class App extends Application {

    private List<String> mediaFileList;
    public enum Mode{Override,Move,Copy};
    public static String datePatern = "yyyy-MM-dd-HH'h'mm'm'ss's'";
    public static String separator = "_";
    public static String postfix="";
    public static Preferences prefs = Preferences.userNodeForPackage(App.class);
    public static String locale;
    public static int ModeInt;
    public static String OutputDirectory = "";
    public static String appVersion="v1.0";






    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));

        locale = prefs.get("Lang", "en");
        datePatern=prefs.get("DatePattern", "yyyy-MM-dd-HH'h'mm'm'ss's'");
        ModeInt = prefs.getInt("Mode",1);
        OutputDirectory = prefs.get("OutputDirectory","");
        fxmlLoader.setResources(ResourceBundle.getBundle("variables", new Locale(locale)));
        //fxmlLoader.setResources(ResourceBundle.getBundle("variables"));
        Scene scene = new Scene(fxmlLoader.load(), 650, 500);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/pl/marcinsachs/date2name/images/icon.png")));
        stage.setTitle("Date2Name");

        stage.setScene(scene);
        stage.show();


    }






    public static void main(String[] args) throws IOException{
        launch();

            //Operations.MakeChanges(Mode.Copy, datePatern, separator, postfix);
    }






}