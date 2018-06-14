/*
 * Copyright powelle
 */
package contactscalendar;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.*;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Pair;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.stage.Stage;

/**
 * FXML ContactsCalendarController class
 * controls login screen (Login.fxml)
 * @author Elaine Powell
 */
public class ContactsCalendarController implements Initializable
{
    public static String user;
    @FXML private Label invalidLabel;
    @FXML private Label locationLabel;
    @FXML private Text loginText;
    @FXML private TextField userNameField;
    @FXML private TextField passWordField;
    @FXML private Button enterButton;
    ZoneId zid = ZoneId.systemDefault();
    //H vs h is difference between 24 hour vs 12 hour format.
    //DateTimeFormatters are all set here
    public static DateTimeFormatter fullformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");
    public static DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm");
    public static DateTimeFormatter ymdformatter = DateTimeFormatter.ofPattern("yyyy:MM:dd");
    public static DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:mm");
    //protected static String loginLogFile = "c:\\loginLog.txt";
    //protected static String loginLogFile = "C:/Users/elaine.powell/loginLog.txt";
    protected static String loginLogFile = "C:\\Users\\Student\\Desktop\\loginLog.txt";
    public static String driverManagerString = "jdbc:mysql://db4free.net:3307/?verifyServerCertificate=false&useSSL=true&user=powelle&password=gecko69";
    
    /**
    * handleButtonAction is called when enter button (enterButton) is clicked
    * @throws IOException
    */
    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException
    {
        // ** handles login button click ** //
        Parent mainParent = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene MainScene = new Scene(mainParent);
        MainScene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        invalidLabel.setVisible(false);
           
        if (credentialsValid())
        {
            primaryStage.setScene(MainScene);
            MainScene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
            primaryStage.show();
        }
        
        else
        {
            userNameField.clear();
            passWordField.clear();
            messageBox();
            invalidLabel.setVisible(true);
        }
    }
    
    /**
     * credentialsValid verifies that user and password entered are in database
     * calls getUserSchedule for user's appointments from database
     * calls writeToLogFile if valid
     * @throws IOException
     */
    private boolean credentialsValid() throws IOException
    {
        String userEntered = userNameField.getText();
        String passwordEntered = passWordField.getText();
        boolean accept = false;
        Connection manager = null;
        PreparedStatement pstmt = null;
        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            String query = "{CALL powellcontacts.login(?, ?)}";
            pstmt = manager.prepareStatement(query);
            pstmt.setString(1, userEntered);
            pstmt.setString(2, passwordEntered);
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next())
            {
                if (resultSet.getString("userName") != null && resultSet.getString("password") != null)
                { 
                    String userName = resultSet.getString("userName");
                    String password = resultSet.getString("password");
                    accept = true;
                    user = userName;
                }
            }
            resultSet.close();
            pstmt.close();
            manager.close();
        }
        catch (Exception e)
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        getUserSchedule();
        writeToLogFile(userEntered);
        return accept;
    }
    
    /**
     * messageBox - message pop-up if there is a login error 
     */
    public void messageBox()
    {
        String name = "contactscalendar.Bundle";
        ResourceBundle messages = ResourceBundle.getBundle(name, ContactsCalendar.currentLocale);
        
        Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle(messages.getString("loginError"));
            dialog.setHeaderText(messages.getString("incorrectUser"));
            dialog.setContentText(messages.getString("tryAgain"));
            ButtonType buttonTypeOk = new ButtonType(messages.getString("okBtn"), ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk);
            dialog.initStyle(StageStyle.UTILITY);
            dialog.showAndWait();
            dialog.setOnCloseRequest((event) -> { event.consume(); });
    }
    
    /**
     * writeToLogFile gathers login data (user, timeStamp) for loginLog file
     * and sends loginText
     * @param loginText - String of login user
     * @throws IOException
     */
    public void writeToLogFile(String loginText) throws IOException
    {
        ZoneId currentZone = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();        
        LocalDateTime localDayDate = LocalDateTime.from(now.atZone(currentZone));
        String dayDate = (fullformatter.format(localDayDate));        
        FileWriter loginWriter = new FileWriter(loginLogFile, true);
        loginWriter.write("Time: " + dayDate + " User: " + loginText + "\n\n  ");
        loginWriter.flush();
        loginWriter.close();
    }
    
// ****** Reminders *********** //
    Timer timer = new Timer();
    
    Appointment nextAppt = new Appointment();
    String apptData;
    
    /**
     * showNotifications calls getTimeToAppt
     * returns local time from database UTC time
     * calls showReminderAlert if time to appointment <= 30 mins
     */
    private void showNotifications()
    {
        long timeToAppt = getTimeToAppt();

        if (timeToAppt <= 30)
        Platform.runLater(() -> showReminderAlert(apptData, timeToAppt));
    }
    
    /**
     * showReminderAlert creates alert box and displays next appointment
     * @param reminderText - String apptData customer, apptType
     * @param timeToAppt - long calculated time before next appointment scheduled
     */
    private void showReminderAlert(String reminderText, long timeToAppt)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reminder");
        alert.setHeaderText("Appointment in " + timeToAppt + " mins.");
        alert.setContentText(reminderText);
        alert.initStyle(StageStyle.UTILITY);
        // Display alert
        alert.showAndWait();
        alert.setOnCloseRequest((event) -> { event.consume(); });
    }
    
    /**
     * getUserSchedule calls database (stored procedure consultant_daily)
     * to get next appointment for user
     * sets nextApptStart and apptData with next appointment
     * calls showNotifications
     */
    public void getUserSchedule()
    {
        PreparedStatement pstmt = null;
        boolean accept = false;
        Connection manager = null;
        ZonedDateTime zdtTime = LocalDateTime.now().atZone(zid);
        ZonedDateTime utcTime = zdtTime.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime ldtTime = utcTime.toLocalDateTime();
        String stTime = ldtTime.format(fullformatter);
        Timestamp tsTime = Timestamp.valueOf(stTime);
                        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            String query = "{CALL powellcontacts.consultant_daily(?, ?)}";
            pstmt = manager.prepareStatement(query);
            pstmt.setString(1, user);
            pstmt.setTimestamp(2, tsTime);
            ResultSet result = pstmt.executeQuery();
                      
            //adds data to variable
            while(result.next())
            {                
                if (result.getString("start") != null)
                {
                    nextAppt.setStartTime(result.getString(2));
                    nextAppt.setCustomerName(result.getString(5));
                    nextAppt.setApptType(result.getString(4));
                    
                    apptData = ("Customer: " + nextAppt.getCustomerName() + "\n" + " Appt. Type: " + nextAppt.getApptType());
                    accept = true;
                }
                showNotifications();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Error on Building Data");             
        }
    }
    
    /**
     * getTimeToAppt gets start time and
     * calculates how many minutes until next appointment
     */
    public long getTimeToAppt()
    {
        long timeToAppt = 0;
        // ** get appointment time from Appointment object
        String rowDate = nextAppt.getStartTime().substring(0, 16);
        LocalDateTime start = LocalDateTime.parse(rowDate, datetimeformatter);
        
        // ** sends local zone startTime to apptData string
        String trimmedLocalStart = start.format(hourFormatter);
        apptData = ("Start time: " + trimmedLocalStart + "\n" + apptData);
        
        // ** subtract that time from currentTime
        LocalDateTime now = LocalDateTime.now();
        timeToAppt = ChronoUnit.MINUTES.between(now, start);
        return timeToAppt;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }    
}