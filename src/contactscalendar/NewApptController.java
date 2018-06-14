/*
 * Copyright powelle
 */
package contactscalendar;

import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

/**
 * FXML NewApptController class
 * controls New Appointment screen (newApptPopUp.fxml)
 * @author Elaine Powell
 */
public class NewApptController implements Initializable
{
    @FXML private DatePicker apptDatePicker;
    @FXML private TextField startTimeEntered;
    @FXML private TextField apptLengthField;
    @FXML private TextField apptTypeField;
    @FXML private TextField customerNameField;
    @FXML private TextField consultantField;
    @FXML private Button addAppointmentBtn;
    @FXML private Button cancelAppointmentBtn;
    @FXML private Label correctTimeLbl;
    @FXML private Label requiredFieldsLbl;
    @FXML private Label apptOverlapLbl;
    @FXML private Label notBusinessHoursLbl;
    @FXML private Label incorrectNamesLbl;
    @FXML private Label errorLbl;
    
    private static final String TIME_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
    
    String user = ContactsCalendarController.user;
    String driverManagerString = ContactsCalendarController.driverManagerString;
    String paddedStart;
    Date ApptEndTime;
    String date;
        
    ZoneId zid = ZoneId.systemDefault();
    DateTimeFormatter fullformatter = ContactsCalendarController.fullformatter;
    DateTimeFormatter datetimeformatter = ContactsCalendarController.datetimeformatter;
    DateTimeFormatter ymdformatter = ContactsCalendarController.ymdformatter;
    DateTimeFormatter hourFormatter = ContactsCalendarController.hourFormatter;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        
    /**
     * Called by add appointment button (addAppointmentBtn)
     * Validates data entered by user in textFields
     *    all required data must be entered - dataEntered function
     *    all times must be valid in this format '00:00' - validTimeStrings function
     *    times must be within business hours - withinBusinessHours function
     *    appointment time must not overlap previously entered appointments - noOverlappingAppt function
     * then creates an Appointment object and sends it to the saveAppointment function.
     */
    @FXML
    private void addAppointment(ActionEvent event) throws IOException, ParseException
    {
        correctTimeLbl.setVisible(false);
        requiredFieldsLbl.setVisible(false);
        apptOverlapLbl.setVisible(false);
        notBusinessHoursLbl.setVisible(false);
        incorrectNamesLbl.setVisible(false);
        errorLbl.setVisible(false);
        
        if (dataEntered())
        {
            if (validTimeStrings())
            {
              if (withinBusinessHours())
              {
                if (noOverlappingAppt())
                {
                    LocalDate day = apptDatePicker.getValue();
                    String stEntered = startTimeEntered.getText();
                    String paddedStart = "00000".substring(stEntered.length()) + stEntered;
                    String length = apptLengthField.getText();
                    String paddedLength = "00000".substring(length.length()) + length;

                    String apptType = apptTypeField.getText();
                    String customerName = customerNameField.getText();
                    String consultant = consultantField.getText();

                    String[] dates = getTimes(day, paddedStart, paddedLength);
                    String startTime = dates[0];
                    String endTime = dates[1];

                    Appointment newAppt = new Appointment(startTime, endTime, apptType, customerName, consultant);
                    newAppt.setUTCStartTime(startTime);
                    newAppt.setUTCEndTime(endTime);
                    saveAppointment(newAppt);
                    addAppointmentBtn.getScene().getWindow().hide();
                }
                else
                {
                    apptOverlapLbl.setVisible(true);
                }
              }
              else
              {
                  notBusinessHoursLbl.setVisible(true);
              }
            }
            else
            {
                correctTimeLbl.setVisible(true);
            }
        }
        else
        {
            requiredFieldsLbl.setVisible(true);
        }
    }

    /**
     * cancelAppointment called when cancel button (cancelAppointmentBtn) is clicked
     * cancels appointment creation and closes Appointment pop-up window
     */
    @FXML
    private void cancelAppointment(ActionEvent event) throws IOException
    {
        cancelAppointmentBtn.getScene().getWindow().hide();
    }
    
// ** FUNCTIONS **//
    /**
     * Verifies all required data is entered in textFields
     * @return boolean false if any of the data is empty otherwise true
     */
    private boolean dataEntered()
    {
        ArrayList<String> dataFromFields = new ArrayList<>();
        
        dataFromFields.add(apptDatePicker.getValue().toString());
        dataFromFields.add(startTimeEntered.getText());
        dataFromFields.add(apptLengthField.getText());
        dataFromFields.add(apptTypeField.getText());
        dataFromFields.add(customerNameField.getText());
        dataFromFields.add(consultantField.getText());
        
        //dataFromFields.forEach((fields) -> { fields.isEmpty(); return false });
        for (String fields : dataFromFields)
        {
            if (fields.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * time data verified this format '00:00'
     * @return boolean true if time is valid otherwise false
     */
    private boolean validTimeStrings()
    {
        correctTimeLbl.setVisible(false);
        errorLbl.setVisible(false);
        String stEntered = startTimeEntered.getText();
        String length = apptLengthField.getText();
        boolean validated = false;
        
        if (stEntered.matches(TIME_PATTERN) && length.matches(TIME_PATTERN))
        {
            validated = true;
        }
        
        return validated;
    }

    /**
     * verifies that the time selected is within business hours
     * @return boolean true if appointment time within 8:00 AM - 18:00 PM else false
     * @throws ParseException
     */
    private boolean withinBusinessHours() throws ParseException
    {   
        notBusinessHoursLbl.setVisible(false);
        errorLbl.setVisible(false);
        LocalDate day = apptDatePicker.getValue();
        String stEntered = startTimeEntered.getText();
        paddedStart = "00000".substring(stEntered.length()) + stEntered;
        String length = apptLengthField.getText();
        String paddedLength = "00000".substring(length.length()) + length;
        
        //** date doesn't matter as business hours are same on all days **//
        date = day.toString();
        /*int year = Integer.valueOf(date.substring(0, 4));
        int month = Integer.valueOf(date.substring(5, 7));
        int dayNumber = Integer.valueOf(date.substring(8));*/
        
        //** Business hours start and end **//
        Date businessStartTime = dateFormat.parse("08:00"); // 8:00 AM
        Date businessEndTime = dateFormat.parse("18:00"); //6:00 PM
        
        int hour = Integer.valueOf(paddedStart.substring(0, 2));
        int minute = Integer.valueOf(paddedStart.substring(3));
        String startString = (hour + ":" + minute);
        Date ApptStartTime = dateFormat.parse(startString);
        
        int lengthHour = Integer.valueOf(paddedLength.substring(0, 2));
        int lengthMins = Integer.valueOf(paddedLength.substring(3));
        String endString = ((hour + lengthHour) + ":" + (minute + lengthMins));
        ApptEndTime = dateFormat.parse(endString);
        
        if ((ApptStartTime.before(businessStartTime)) || (ApptEndTime.after(businessEndTime)))
        {
            return false;
        }
        
        else
        {
            return true;
        }
    }

    /**
     * verifies that appointment time does not conflict with another appointment
     * calls stored procedure - appointment_time_overlap
     * which gets all the times that fall between the startTime and endTime
     * @return boolean true if no appt times between start and end, otherwise false.
     * @throws ParseException 
     */
    public boolean noOverlappingAppt() throws ParseException
    {
        apptOverlapLbl.setVisible(false);
        errorLbl.setVisible(false);
        Connection manager = null;
        Boolean noOverlap = true;
        
        String startTime = (date + " " + paddedStart);
        LocalDateTime ApptStartTime = LocalDateTime.parse(startTime, datetimeformatter);
        String stringStartTime = ApptStartTime.toString().replace("T", " ");
        
        String endTimeHour = ApptEndTime.toString();
        String justEndHours = endTimeHour.substring(11, 16);
        String endTime = (date + " " + justEndHours);
        LocalDateTime ldtEndTime = LocalDateTime.parse(endTime, datetimeformatter);
        String stringEndTime = ldtEndTime.toString().replace("T", " ");;
        
        String query = "{CALL powellcontacts.appointment_time_overlap(?, ?)}";
        
        Appointment newAppt = new Appointment();
        newAppt.setUTCStartTime(stringStartTime);
        newAppt.setUTCEndTime(stringEndTime);
        PreparedStatement pstmt = null;
        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            pstmt = manager.prepareStatement(query);
            pstmt.setTimestamp(1, newAppt.getUTCStartTime());
            pstmt.setTimestamp(2, newAppt.getUTCEndTime());
            
            ResultSet result = pstmt.executeQuery();
            System.out.println("result: " + result);
            
            if (!result.isBeforeFirst())
            {
                noOverlap = true;
            }

            else
            {
                noOverlap = false;  
            }
        }
        
        catch (Exception e)
        {
            errorLbl.setVisible(true);
        }
        return noOverlap;
    }

    /**
     * takes data entered and converts it to localTime timestamps
     * @param day - apptDatePicker value
     * @param stEntered - start time
     * @param length - length of appointment
     * @return times[] Strings with formatted time data
     * @throws ParseException 
     */
    private String[] getTimes(LocalDate day, String stEntered, String length) throws ParseException
    {
        String[] times = null;
        
        try
        {
            String stDay = day.toString() + " " + stEntered + ":00";
            LocalDateTime localStart = LocalDateTime.parse(stDay, fullformatter);
            
            long hours = Long.parseLong(length.substring(0, 2));
            long mins = Long.parseLong(length.substring(3));
            long totalMins = mins + (hours * 60);
            LocalDateTime localEnd = LocalDateTime.parse(stDay, fullformatter).plusMinutes(totalMins).plusSeconds(0);

            ZonedDateTime zdtStart = localStart.atZone(zid);
            String stStart = zdtStart.format(fullformatter);
            
            ZonedDateTime zdtEnd = localEnd.atZone(zid);
            String ending = zdtEnd.format(fullformatter);
            times = new String[]{stStart, ending};
        }
        catch (Exception e)
        {
            errorLbl.setVisible(true);
        }
        return times;
    }
    
    /**
     * takes Appointment object and saves its data to database
     * using new_appointment stored procedure
     * @param newAppt 
     */
    private void saveAppointment(Appointment newAppt)
    {
        Connection manager = null;
        String query = "{CALL powellcontacts.new_appointment(?, ?, ?, ?, ?, ?)}";
        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            CallableStatement stmt = manager.prepareCall(query);
            stmt.setString(1, newAppt.getCustomerNameString());
            stmt.setString(2, newAppt.getApptTypeString());
            stmt.setString(3, newAppt.getConsultantString());
            stmt.setTimestamp(4, newAppt.getUTCStartTime());
            stmt.setTimestamp(5, newAppt.getUTCEndTime());
            stmt.setString(6, user);
            
            stmt.execute();
            stmt.close();
            manager.close();
                        
            //** add new appt to calendars
            String dateSend = date + " 11:11:11";
            CalendarController.getCurrentCalendarController().getLbl(dateSend);
        }
        
        catch (Exception e)
        {
            //System.err.println( e.getClass().getName() + ": " + e.getMessage());
            incorrectNamesLbl.setVisible(true);
        }
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }
}