/*
 * Copyright powelle
 */
package contactscalendar;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML MainMenuController class
 * controls the main menu screen (MainMenu.fxml) with buttons to access
 *      Customer screen
 *      Calendar screen
 *      Reports screen
 * or to logout of application
 * @author Elaine Powell
 */
public class MainMenuController implements Initializable
{
    @FXML public static Label counterLbl;
    
    /**
     * customerAction called when add/edit customer button clicked
     * opens customer add/edit screen
     * @throws IOException
     */
    @FXML
    private void customerAction(ActionEvent event) throws IOException
    {
        Parent customerParent = FXMLLoader.load(getClass().getResource("Customer.fxml"));
        Scene CustomerScene = new Scene(customerParent);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(CustomerScene);
        CustomerScene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        primaryStage.show();
    }
    
    /**
     * calendarAction called when calendar button clicked
     * opens the appointment calendar
     * @throws IOException
     */
    @FXML
    private void calendarAction(ActionEvent event) throws IOException
    {
        Parent calendarParent = FXMLLoader.load(getClass().getResource("Calendar.fxml"));
        Scene CalendarScene = new Scene(calendarParent);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(CalendarScene);
        CalendarScene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        primaryStage.show();
    }

    /**
     * reportsAction called when reports button is clicked
     * opens the reports
     * @throws IOException
     */
    @FXML
    private void reportsAction(ActionEvent event) throws IOException
    {
        Parent reportsParent = FXMLLoader.load(getClass().getResource("Reports.fxml"));
        Scene ReportsScene = new Scene(reportsParent);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(ReportsScene);
        ReportsScene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        primaryStage.show();
    }

    /**
     * logoutAction called when logout button is clicked
     * logs out of the application
     * @throws IOException
     */
    @FXML
    private void logoutAction(ActionEvent event) throws IOException
    {
        System.exit(0);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        
    } 
}