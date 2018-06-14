/*
 * Copyright powelle
 */
package contactscalendar;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
/**
 * FXML DailyApptController class
 * controls appointment pop-up screen (dailyAppts.fxml)
 * @author Elaine Powell
 */
public class DailyApptController implements Initializable
{
    @FXML private GridPane dailyGrid;
    @FXML private TableView dailyApptTV;
    @FXML private Button closeBtn;
    boolean isWeek = CalendarController.isWeek;
    
    String day = CalendarController.day;
    Boolean splitMonthWeek = CalendarController.splitMonthWeek;
    String driverManagerString = ContactsCalendarController.driverManagerString;
    
    private ObservableList<ObservableList> data;
    
    ZoneId zid = ZoneId.systemDefault();
    //H vs h is difference between 24 hour vs 12 hour format.
    DateTimeFormatter df = ContactsCalendarController.datetimeformatter;
    DateTimeFormatter fullFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter yearMonthDayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter hourFormatter = ContactsCalendarController.hourFormatter;
    
// ** METHODS **//
    /**
     * showDailySchedule called from tableView - dailyApptTV
     * calls daily_appointments stored procedure and gets daily appointments from db
     * adds data from database to observable list
     * @throws ParseException, SQLException
     */
    @FXML
    private void showDailySchedule() throws ParseException, SQLException
    {
        Connection manager = null;
        PreparedStatement pstmt = null;
        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            String query = "{CALL powellcontacts.daily_appointments(?)}";
            
            pstmt = manager.prepareStatement(query);
            pstmt.setString(1, getDate());
            ResultSet result = pstmt.executeQuery();
            ObservableList<Appointment> row = FXCollections.observableArrayList();
            
            //adds data to observable list
            while (result.next())
            {
                Appointment appt = new Appointment();
                appt.setStartTime(result.getString(1));
                appt.setEndTime(result.getString(2));
                appt.setApptType(result.getString(3));
                appt.setCustomerName(result.getString(4));
                appt.setConsultant(result.getString(5));
                row.add(appt);
            }
            // column names
            TableColumn <Appointment, String> startTime = new TableColumn <> ("Start Time");
            startTime.setCellValueFactory(new PropertyValueFactory("startTime"));

            TableColumn <Appointment, String> endTime = new TableColumn <> ("End Time");
            endTime.setCellValueFactory(new PropertyValueFactory("endTime"));
            
            TableColumn <Appointment, String> apptType = new TableColumn <> ("Appt. Type");
            apptType.setCellValueFactory(new PropertyValueFactory("apptType"));

            TableColumn <Appointment, String> customer = new TableColumn <> ("Customer");
            customer.setCellValueFactory(new PropertyValueFactory("customerName"));

            TableColumn <Appointment, String> consultant = new TableColumn <> ("Consultant");
            consultant.setCellValueFactory(new PropertyValueFactory("consultant"));
            
            dailyApptTV.getColumns().setAll(startTime, endTime, apptType, customer, consultant);
            dailyApptTV.setItems(row);
            pstmt.close();
            manager.close();
          }
          catch (Exception ex)
          {
              Logger.getLogger(DailyApptController.class.getName()).log(Level.WARNING, null, ex);
          }
    }
    
    /**
     * closeAppointments called when close button (closeBtn) is clicked
     * closes scene
     */
    @FXML
    private void closeAppointments(ActionEvent event)
    {
        closeBtn.getScene().getWindow().hide();
    }
    
// ** FUNCTIONS **//
    /**
     * gets date from month mouseClick in CalendarController class
     * @return dayDate - String formatted local datetime
     * @throws ParseException
     */
    private String getDate() throws ParseException
    {
        LocalDate currentMonth = LocalDate.now(zid);
        
        String stringMonth = (yearMonthDayFormat.format(currentMonth));
        stringMonth = stringMonth.substring(0, 8) + day + " 00:00:00";
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dayDateDate = formatter.parse(stringMonth);
        
        //** subtract a day
        LocalDateTime localDayDate = LocalDateTime.from(dayDateDate.toInstant().atZone(zid)).minusDays(1);
        String dayDate = (fullFormat.format(localDayDate));
        return dayDate;
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        try
        {
            showDailySchedule();
        }
        catch (ParseException ex)
        {
            Logger.getLogger(DailyApptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex)
        {
            Logger.getLogger(DailyApptController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}