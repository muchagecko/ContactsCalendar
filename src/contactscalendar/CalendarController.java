/*
 * Copyright powelle
 */
package contactscalendar;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
/**
 * FXML Calendar Controller class
 * controls Calendar screen (Calendar.fxml)
 * @author Elaine Powell
 */
public class CalendarController implements Initializable
{
    @FXML private Text actiontarget;
    @FXML private Button newApptBtn;
    @FXML private GridPane gridCalendar;
    @FXML private Label monthLbl;
    
    private Label[] dayLbl = new Label[42];
    
    public static String day;
    String driverManagerString = ContactsCalendarController.driverManagerString;
    
    private ObjectProperty<YearMonth> month = new SimpleObjectProperty<>();
    private ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.ENGLISH); //(Locale.getDefault())
    
    private ObservableList<ObservableList> data;
    
    public int monthStartAdjust;
    public static boolean splitMonthWeek = false;
    public static boolean isWeek;
    int weekCounter = 0;
    
    DateTimeFormatter fullformatter = ContactsCalendarController.fullformatter;
    DateTimeFormatter datetimeformatter = ContactsCalendarController.datetimeformatter;
    DateTimeFormatter ymdformatter = ContactsCalendarController.ymdformatter;
    DateTimeFormatter hourFormatter = ContactsCalendarController.hourFormatter;
    ZoneId zid = ZoneId.systemDefault();
    
    public static CalendarController currentCalendarController;
    
    public static CalendarController getCurrentCalendarController()
    {
        return currentCalendarController;
    }

// ** METHODS **//

    /**
     * newAppointment called when new appointment button clicked
     * opens new pop-up with fields to enter new appointment
     * @throws IOException
     */
    @FXML
    private void newAppointment(ActionEvent event) throws IOException
    {
        Stage newApptStage = new Stage();           
        Parent root;
        root = FXMLLoader.load(getClass().getResource("newApptPopUp.fxml"));
        newApptStage.setScene(new Scene(root));
        newApptStage.setTitle("New Appointment");
        newApptStage.initModality(Modality.APPLICATION_MODAL);
        newApptStage.initOwner(newApptBtn.getScene().getWindow());
        newApptStage.showAndWait();
    }
    
    /**
     * backToMain is called when back to main menu button is clicked
     * loads main menu scene
     * @throws IOException
     */
    @FXML
    private void backToMain(ActionEvent event) throws IOException
    {
        Parent mainParent = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene MainScene = new Scene(mainParent);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(MainScene);
        MainScene.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        primaryStage.show();
    }

    /**
     * logoutAction called when logout button clicked
     * application closed
     * @throws IOException
     */
    @FXML
    private void logoutAction(ActionEvent event) throws IOException
    {
        System.exit(0);
    }

    /**
     * showDailyAppointments called when calendar date buttons are clicked
     * opens dailyAppts pop-up with appointments on selected date
     * @throws IOException
     */
    @FXML
    private void showDailyAppointments() throws IOException
    {
        Stage dailyApptStage = new Stage();           
        Parent root;
        root = FXMLLoader.load(getClass().getResource("dailyAppts.fxml"));
        dailyApptStage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("contactsCalendarFontStyles.css").toExternalForm());
        dailyApptStage.setTitle("Daily Appointments");
        dailyApptStage.initModality(Modality.APPLICATION_MODAL);
        dailyApptStage.showAndWait();
    }    
    
// ** FUNCTIONS **//

    /**
     * setCalendarDays creates and displays current month calendar
     * calls datesWithAppts to get dates with appointments
     */
    public void setCalendarDays()
    {
        // ** Month Header ** //
        YearMonth todaysDate = YearMonth.now(zid);
        String todaysDateSt = todaysDate.toString();
        monthLbl.setText(todaysDateSt);
        
        // ** Month GridPane ** //
        GridPane calendar = new GridPane();
        calendar.setStyle("-fx-background-color: palegreen; -fx-alignment: center; -fx-border-insets: 5; -fx-border-width: 3;");
        calendar.setAlignment(Pos.CENTER);
        calendar.setHgap(36);
        calendar.setVgap(14);
        
        // ** Labels for dates ** //
        for (int i = 0; i < 42; i++)
        {
            dayLbl[i] = new Label();
            dayLbl[i].setTextAlignment(TextAlignment.RIGHT);
        }
        
        // ** adds labels into gridpanes ** //
        for (int i = 0; i < 42; i++)
        {
            calendar.add(dayLbl[i], i % 7, i / 7);
        }
        
        //** add grids to maingrid **//
        gridCalendar.add(calendar, 0, 3);
        
        // ** find current dates ** //
        LocalDate localDate = LocalDate.now(zid);
        LocalDate firstDay = localDate.with(TemporalAdjusters.firstDayOfMonth());
        DayOfWeek firstDotw = firstDay.getDayOfWeek();
        LocalDate monthEnd = localDate.with(TemporalAdjusters.lastDayOfMonth());
        String endMonthString = monthEnd.toString();
        int endMonthNumber = Integer.valueOf(endMonthString.substring(8));
        
        TemporalField firstDayOfWeek = WeekFields.of(Locale.US).dayOfWeek();
        LocalDate thisWeekStart = localDate.with(firstDayOfWeek, 1);
        String dayDate = (ymdformatter.format(thisWeekStart));
        
        // ** Sets day adjustments for calendar by weekday start ** //
        switch (firstDotw)
        {
            case MONDAY:
                monthStartAdjust = 2;
                break;
                
            case TUESDAY:
                monthStartAdjust = 3;
                break;
            
            case WEDNESDAY:
                monthStartAdjust = 4;
                break;
                
            case THURSDAY:
                monthStartAdjust = 5;
                break;
                    
            case FRIDAY:
                monthStartAdjust = 6;
                break;
                
            case SATURDAY:
                monthStartAdjust = 7;
                break;
                
            default:
                monthStartAdjust = 1;
        }

        Calendar prevCalendarData = new GregorianCalendar();
        prevCalendarData.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevCalendarData.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        //*** Previous month ***//
        for (int i = 0; i < monthStartAdjust - 1; i++)
        {
            dayLbl[i].setTextFill(Color.WHITE);
            dayLbl[i].setText(daysInPrevMonth - monthStartAdjust + 2 + i + "");
        }
        //*** current month ***//
        for (int i = 1; i <= endMonthNumber; i++)
        {
            dayLbl[i - 2 + monthStartAdjust].setTextFill(Color.BLACK);
            dayLbl[i - 2 + monthStartAdjust].setText(i + "");
            dayLbl[i - 2 + monthStartAdjust].addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, eventHandler);
        }
        //*** Next month ***//
        int j = 1;
        for (int i = endMonthNumber - 1 + monthStartAdjust; i < 42; i++)
        {
            dayLbl[i].setTextFill(Color.WHITE);
            dayLbl[i].setText(j++ + "");
        }
        
        datesWithAppts();
    }
    
    /**
     * Finds monthly dates in database that have appointments
     * calls dates_with_appointments stored procedure
     * dates with appointments are sent as Set to colorApptDates
     */
    public void datesWithAppts()
    {
        Connection manager = null;
        PreparedStatement pstmt = null;
        
        ZonedDateTime zdtTime = LocalDateTime.now().atZone(zid);
        ZonedDateTime utcTime = zdtTime.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime ldtTime = utcTime.toLocalDateTime();
        String stTime = ldtTime.format(fullformatter);
        Timestamp tsTime = Timestamp.valueOf(stTime);
 
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            String query = "{CALL powellcontacts.dates_with_appointments(?)}";
            pstmt = manager.prepareStatement(query);
            pstmt.setTimestamp(1, tsTime);
            
            ResultSet result = pstmt.executeQuery();
            
            Set<String> datesList = new TreeSet<>();
            
            while (result.next())
            {
                String startDate = result.getString("Date").substring(0, 19);
                LocalDateTime localStart = LocalDateTime.parse(startDate, fullformatter);
                ZonedDateTime utcStart = localStart.atZone(ZoneId.of("UTC"));
                ZonedDateTime zdtStart = utcStart.withZoneSameInstant(zid);
                String date = zdtStart.format(ymdformatter);
                datesList.add(date);
            }
            result.close();
            
            //** convert to just days - no duplicates using a set
            Set<Integer> justDates = new HashSet<>();
            datesList.forEach((dates) -> { dates = dates.substring(8, 10); justDates.add(Integer.parseInt(dates)); });
            colorApptDates(justDates);
        }
        catch(Exception ex)
        {
            Logger.getLogger(DailyApptController.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    /**
     * Dates that have appointments are set to magenta
     * @param justDates - Set<Integer> of dates that have appointments in database
     */
    private void colorApptDates(Set<Integer> justDates)
    {
        Set<Integer> dates = justDates;
        dates.forEach((date) -> { dayLbl[date + monthStartAdjust - 2].setTextFill(Color.MAGENTA); });
    }

    EventHandler<javafx.scene.input.MouseEvent> eventHandler = new EventHandler<javafx.scene.input.MouseEvent>()
    {
        /**
         * adds click event to date labels on calendar
         */
        @Override
        public void handle(javafx.scene.input.MouseEvent e)
        {
            day = e.getSource().toString();
            Pattern pttn = Pattern.compile("'([^' ]+)'");
            Matcher match = pttn.matcher(day);
            
            while (match.find())
            {
                day = match.group(1);
            }
            
            try
            {
                showDailyAppointments();
                isWeek = true;
            }
            catch (IOException ex)
            {
                Logger.getLogger(CalendarController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    
    /**
     * getLbl called by newApptController to color newly added appointment dates
     * @param day - String variable of date added
     */
    public void getLbl(String day)
    {
        int dayNumber = Integer.parseInt(day.substring(8, 10));
        int dayMonth = Integer.parseInt(day.substring(5, 7));
        ZonedDateTime now = ZonedDateTime.now(zid);
        String nowString = now.toString();
        int currentMonth = Integer.parseInt(nowString.substring(5, 7));
        TemporalField weekFields = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        
        LocalDateTime dayLDT = LocalDateTime.parse(day, fullformatter);
        int dayWeek = dayLDT.get(weekFields);
        
        LocalDate day2 = LocalDate.now();
        TemporalField fieldUS = WeekFields.of(Locale.US).dayOfWeek();
        String formattedString = (day2.with(fieldUS, 1)).format(ymdformatter);
        
        if (dayMonth == currentMonth)
        {
            dayLbl[dayNumber + monthStartAdjust - 2].setTextFill(Color.MAGENTA);
        }
        else
        {
        }
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        setCalendarDays();
        currentCalendarController = this;
    }
}