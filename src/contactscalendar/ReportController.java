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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FXML ReportController class
 * controls the reports screen (Reports.fxml) with 3 reports available
 *      Monthly Appt. Types
 *      Consultant Schedule
 *      Next Appointment
 * or return to main menu or to logout of application
 * @author Elaine Powell
 */
public class ReportController implements Initializable
{
    @FXML private ChoiceBox reportChBox;
    @FXML private TableView reportsTV;
    @FXML private Label errorLbl;

    private ObservableList<ObservableList> data;
    
    String user = ContactsCalendarController.user;
    String driverManagerString = ContactsCalendarController.driverManagerString;
    
    ZoneId zid = ZoneId.systemDefault();
    //H vs h is difference between 24 hour vs 12 hour format.
    DateTimeFormatter datetimeformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm");
        
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        reportChBox.getItems().removeAll(reportChBox.getItems());
        reportChBox.getItems().addAll("Monthly Appt. Types", "Consultant Schedule", "Next Appointment");
    }
    
// ** METHODS **//
    /**
     * reportChosen called for report ChBox
     * gets selection from ChBox and selects stored procedure
     * sends selection String to addReportData
     * @param event - ChBox selection
     * @throws IOException
     */
    @FXML
    public void reportChosen(ActionEvent event) throws IOException
    {
        errorLbl.setVisible(false);
        reportsTV.getItems().clear();
        reportsTV.getColumns().clear();
        int selectedReport = reportChBox.getSelectionModel().getSelectedIndex();
        
        switch(selectedReport)
        {
            case 0: addReportData("{CALL powellcontacts.monthly_appt_types()}");
                break;
            case 1: addReportData("{CALL powellcontacts.consultant_schedule(?, ?)}");
                break;
            case 2: addReportData("{CALL powellcontacts.next_appointment(?, ?)}");
                break;
            default:
                errorLbl.setVisible(true);
        }
    }
    
    /**
     * addReportData called by reportChosen
     * calls stored procedure based on report ChBox choice
     *       monthly_appt_types
     *       consultant_schedule
     *       next_appointment
     * adds data from database to grid
     * @param sqlStoredProcName - name of stored procedure selected
     */
    @FXML
    private void addReportData(String sqlStoredProcName)
    {
        // ** pulls selected report data from database ** //
        Connection manager = null;
        data = FXCollections.observableArrayList();
        PreparedStatement pstmt = null;
        
        // ** Monthly appt types report only ** //
        if (sqlStoredProcName == "{CALL powellcontacts.monthly_appt_types()}")
        {
            try
            {
                manager = DriverManager.getConnection(driverManagerString);
                String query = sqlStoredProcName;
                pstmt = manager.prepareStatement(query);
                ResultSet result = pstmt.executeQuery();

              // column names
              for (int i = 0; i < result.getMetaData().getColumnCount(); i++)
              {
                  final int j = i;
                  System.out.println("FOR LOOP STARTED [" + i + "] [" + j + "]");
                  TableColumn col = new TableColumn(result.getMetaData().getColumnName(i + 1));

                  col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>()
                  {
                      public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param)
                      {
                          return new SimpleStringProperty(param.getValue().get(j).toString());
                      }
                  });
                  reportsTV.getColumns().addAll(col);
              }
              //adds data to observable list
              while(result.next())
              {
                  ObservableList<String> row = FXCollections.observableArrayList();

                  for(int i = 1; i <= result.getMetaData().getColumnCount(); i++)
                  {
                      row.add(result.getString(i));
                  }
                  data.add(row);
              }
              //adds to tableview
              reportsTV.setItems(data);
              }
              catch(Exception e)
              {
                  errorLbl.setVisible(true);
              }
        }
        else // ** other 2 reports - Consultant Schedule & Next Appointment
        {
            try
            {
                manager = DriverManager.getConnection(driverManagerString);
                String query = sqlStoredProcName;
                pstmt = manager.prepareStatement(query);
                pstmt.setString(1, user);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ResultSet result = pstmt.executeQuery();

                ObservableList<Appointment> row = FXCollections.observableArrayList();

                //adds data to observable lists
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

                reportsTV.getColumns().setAll(startTime, endTime, apptType, customer, consultant);
                reportsTV.setItems(row);
              }
              catch (Exception ex)
              {
                  errorLbl.setVisible(true);
              }
        }
    }
    
    /**
     * backToMain called when back to main menu button is clicked
     * main menu scene
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
     * logoutAction called when logout button is clicked
     * @throws IOException
     */
    @FXML
    private void logoutAction(ActionEvent event) throws IOException    
    {
        // ** logs out of application ** //
        System.exit(0);
    }
}