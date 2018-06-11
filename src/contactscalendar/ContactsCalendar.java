/*
 * Copyright powelle
 */
package contactscalendar;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ContactsCalendar Main class
 * 
 * @author Elaine Powell
 */
public class ContactsCalendar extends Application
{
        public static Locale currentLocale = Locale.getDefault();
        //public static Locale currentLocale = new Locale("en", "US");
        //public static Locale currentLocale = new Locale("es", "MX");
    
    @Override
    public void start(Stage stage) throws Exception
    {
        String name = "contactscalendar.Bundle";
    
        ResourceBundle messages = ResourceBundle.getBundle(name, currentLocale);
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"), messages);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}