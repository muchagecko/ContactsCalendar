/*
 * Copyright powelle
 */
package contactscalendar;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Appointment class
 * converts dateFormats to handle Time Zones
 * @author Elaine Powell
 */
public class Appointment
{
    StringProperty startTime;
    StringProperty endTime;
    StringProperty apptType;
    StringProperty customerName;
    StringProperty consultant;
    String startTimeString;
    String endTimeString;
    String apptTypeString;
    String customerNameString;
    String consultantString;
    Timestamp startTimestamp;
    Timestamp endTimestamp;
    
    ZoneId zid = ZoneId.systemDefault();
    DateTimeFormatter fullformatter = ContactsCalendarController.fullformatter;
    DateTimeFormatter datetimeformatter = ContactsCalendarController.datetimeformatter;
    DateTimeFormatter ymdformatter = ContactsCalendarController.ymdformatter;
    DateTimeFormatter hourFormatter = ContactsCalendarController.hourFormatter;
    
    public Appointment()
    {
        
    }
    
    public Appointment(String startTime, String customerName, String apptType)
    {
        this.startTimeString = startTime;
        this.customerNameString = customerName;
        this.apptTypeString = apptType;
    }

    public Appointment(String startTime, String endTime, String apptType, String customerName, String consultant)
    {
        this.startTimeString = startTime;
        this.endTimeString = endTime;
        this.apptTypeString = apptType;
        this.customerNameString = customerName;
        this.consultantString = consultant;
    }
// ** Start Time getters and setters ** //
    public StringProperty startTimeProperty()
    {
        if (startTime == null) startTime = new SimpleStringProperty(this, "startTime");
        return startTime;
    }

    public void setStartTime(String startTime)
    {
        //converts from UTC to local time
        String shortenedStart = startTime.substring(0, 19);
        LocalDateTime localStart = LocalDateTime.parse(shortenedStart, fullformatter);
        ZonedDateTime utcStart = localStart.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtStart = utcStart.withZoneSameInstant(zid);
        String hoursMins = zdtStart.format(datetimeformatter);
        startTimeProperty().set(hoursMins);
    }
    
    public void setUTCStartTime(String startTime)
    {
        //converts from local time to UTC
        LocalDateTime startLDT = LocalDateTime.parse(startTime.substring(0, 16), datetimeformatter);
        ZonedDateTime zdtTime = startLDT.atZone(zid);
        ZonedDateTime utcTime = zdtTime.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime ldtTime = utcTime.toLocalDateTime();
        Timestamp tsTime = Timestamp.valueOf(ldtTime);
        
        this.startTimestamp = tsTime;
    }
    
    public String getStartTime()
    {
        return startTimeProperty().get();
    }
    
    public Timestamp getUTCStartTime()
    {
        return startTimestamp;
    }
    
// ** End Time getters and setters ** //
    public String getEndTime()
    {
        return endTimeProperty().get();
    }
    
    public StringProperty endTimeProperty()
    {
        if (endTime == null) endTime = new SimpleStringProperty(this, "endTime");
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        //converts from UTC to local time
        String shortenedEnd = endTime.substring(0, 16);
        LocalDateTime localEnd = LocalDateTime.parse(shortenedEnd, datetimeformatter);
        ZonedDateTime utcEnd = localEnd.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdtEnd = utcEnd.withZoneSameInstant(zid);
        String hoursMins = zdtEnd.format(datetimeformatter);
        endTimeProperty().set(hoursMins);
    }
    
    public void setUTCEndTime(String endTime)
    {
        //converts from local time to UTC
        LocalDateTime endLDT = LocalDateTime.parse(endTime.substring(0, 16), datetimeformatter);
        ZonedDateTime zdtTime = endLDT.atZone(zid);
        ZonedDateTime utcTime = zdtTime.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime ldtTime = utcTime.toLocalDateTime();
        Timestamp tsTime = Timestamp.valueOf(ldtTime);
        
        this.endTimestamp = tsTime;
    }
    
    public Timestamp getUTCEndTime()
    {
        return endTimestamp;
    }
    
// ** Appt Type getters and setters ** //
    public String getApptType()
    {
        return apptTypeProperty().get();
    }
    
    public String getApptTypeString()
    {
        return apptTypeString;
    }
    
    public StringProperty apptTypeProperty()
    {
        if (apptType == null) apptType = new SimpleStringProperty(this, "apptType");
        return apptType;
    }

    public void setApptType(String apptType)
    {
        apptTypeProperty().set(apptType);
    }

// ** Customer Name getters and setters ** //
    public String getCustomerName()
    {
        return customerNameProperty().get();
    }
    
    public String getCustomerNameString()
    {
        return customerNameString;
    }
    
    public StringProperty customerNameProperty()
    {
        if (customerName == null) customerName = new SimpleStringProperty(this, "customerName");
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        customerNameProperty().set(customerName);
    }

// ** Consultant getters and setters ** //
    public String getConsultant()
    {
        return consultantProperty().get();
    }
    
    public String getConsultantString()
    {
        return consultantString;
    }
    
    public StringProperty consultantProperty()
    {
        if (consultant == null) consultant = new SimpleStringProperty(this, "consultant");
        return consultant;
    }

    public void setConsultant(String consultant)
    {
        consultantProperty().set(consultant);
    }
    
    @Override
    public String toString()
    {
        return getStartTime().toString() + " : " + getEndTime().toString() + " : " + getApptType().toString() + " : " + getCustomerName().toString() + " : " + getConsultant().toString();
    }
}