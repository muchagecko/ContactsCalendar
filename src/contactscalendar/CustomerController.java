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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.ButtonBar;

/**
 * FXML CustomerController class
 * controls customer details screen (Customer.fxml)
 * @author Elaine Powell
 */
public class CustomerController implements Initializable
{
    @FXML private TextField customerNameField;
    @FXML private TextField customerAddress;
    @FXML private TextField customerAddress2;
    @FXML private TextField customerCityField;       
    @FXML TextField customerZipField;
    @FXML private TextField customerCountry;
    @FXML private TextField customerPhoneField;
    @FXML private Label noRecordsLbl;
    @FXML private Label notActiveLbl;
    @FXML private Label zipcodeReqLbl;
    @FXML private Label cityReqLbl;
    @FXML private Label countryReqLbl;
    @FXML private Label databaseErrorLbl;
    @FXML private Label customerEnteredLbl;
    @FXML private Label customerSuccessLbl;
    @FXML private Button editRecordBtn;
    @FXML private Button saveRecordBtn;
    @FXML private Button newRecordBtn;
    @FXML private Button deleteRecordBtn;
    @FXML private Button getCustomerBtn;
    @FXML private Button newSaveBtn;
    @FXML private ButtonBar buttonBar;
    @FXML private Button cancelBtn; 
    String customerNameToEdit;
    String user = ContactsCalendarController.user;
    String driverManagerString = ContactsCalendarController.driverManagerString;

    /**
    * GetCustomerData is called by search button (getCustomerBtn)
    * gets records for customer entered in customerNameField from database by
    * calling search_customer stored procedure
    * fills fields with customer information if found, makes visible noRecordsLbl otherwise
    */
    @FXML
    private void getCustomerData(ActionEvent event)
    {
        turnOffLabels();
        
        String customerNameEntered = customerNameField.getText();
        Boolean recordFound = false;
        
        Connection manager = null;
        PreparedStatement pstmt = null;

        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            String query = "{CALL powellcontacts.search_customer(?)}";
            pstmt = manager.prepareStatement(query);
            pstmt.setString(1, customerNameEntered);
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next())
            {
                if (resultSet.getString("customerName") != null)
                {
                    recordFound = true;
                    String customer = resultSet.getString("customerName");
                    int active = resultSet.getInt("active");
                    customerAddress.setText(resultSet.getString("address"));
                    customerAddress2.setText(resultSet.getString("address2"));
                    customerCityField.setText(resultSet.getString("city"));
                    customerZipField.setText(resultSet.getString("postalCode"));
                    customerCountry.setText(resultSet.getString("country"));
                    customerPhoneField.setText(resultSet.getString("phone"));
                    editRecordBtn.setDisable(false);
                    deleteRecordBtn.setDisable(false);
                    boolean actStatus = (active == 0) ? true : false;
                    notActiveLbl.setVisible(actStatus);
                }
                
                else
                {
                    noRecordsLbl.setVisible(true);
                }
            }
            resultSet.close();
            pstmt.close();
            manager.close();
        }
        
        catch (Exception e)
        {
            databaseErrorLbl.setVisible(true);
        }
        
        if (recordFound == false)
        {
            noRecordsLbl.setVisible(true);
        }
    }

    /**
    * newButtonAction called when new button (newRecordBtn) is clicked
    * clears all the error labels and calls clearCustomerData
    */
    @FXML
    private void newButtonAction(ActionEvent event)
    {
        turnOffLabels();
        clearCustomerData();
        newSaveBtn.setDisable(false);
        deleteRecordBtn.setDisable(true);
        enableFields();
    }

    /**
     * editCustomerData - called when edit button (editRecordBtn) clicked
     * sets buttons and fields when customer data is to be edited
     */
    @FXML
    private void editCustomerData(ActionEvent event)
    {
        turnOffLabels();
        customerNameToEdit = customerNameField.getText();
        saveRecordBtn.setDisable(false);
        deleteRecordBtn.setDisable(false);
        enableFields();
        editRecordBtn.setDisable(true);
    }

    /**
     * saveCustomerData called when save button (saveRecordBtn) is clicked
     * saves customer data entered to database by prepareStatement
     * if data isn't saved, error label made visible
     */
    @FXML
    private void saveCustomerData(ActionEvent event)
    {
        turnOffLabels();
        
        Connection manager = null;
        PreparedStatement pstmt = null;
        
        String[] editedData = getCustomerEnteredData();
        
        String customerNameEdited = editedData[0];
        String addressEdited = editedData[1];
        String address2Edited = editedData[2];
        String cityEdited = editedData[3];
        String postalCodeEdited = editedData[4];
        String countryEdited = editedData[5];
        String customerPhoneEdited = editedData[6];
        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            String query = "{CALL powellcontacts.save_customer(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            pstmt = manager.prepareStatement(query);
            pstmt.setString(1, customerNameToEdit);
            pstmt.setString(2, editedData[0]);
            pstmt.setString(3, user);
            pstmt.setString(4, editedData[6]);
            pstmt.setString(5, editedData[1]);
            pstmt.setString(6, editedData[2]);
            pstmt.setString(7, editedData[3]);
            pstmt.setString(8, editedData[4]);
            pstmt.setString(9, editedData[5]);
            pstmt.execute();
            pstmt.close();
            manager.close();

            saveRecordBtn.setDisable(true);
            deleteRecordBtn.setDisable(true);
            disableFields();
        }
        
        catch (Exception e)
        {
            databaseErrorLbl.setVisible(true);
            //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     * deleteRecord called when delete button (deleteRecordBtn) clicked
     * deletes customer record by setting it inactive in db
     * calls stored procedure inactivate_customer
     * @throws IOException
     */
    @FXML
    private void deleteRecord(ActionEvent event) throws IOException
    {
        turnOffLabels();
        Connection manager = null;
        String query = "{CALL powellcontacts.inactivate_customer(?, ?)}";
        String customerNameEntered = customerNameField.getText();
                        
        try
        {
            manager = DriverManager.getConnection(driverManagerString);
            PreparedStatement stmt = manager.prepareStatement(query);
            stmt.setString(1, customerNameEntered);
            stmt.setString(2, user);
                                            
            stmt.execute();
            stmt.close();
            manager.close();
            deleteRecordBtn.setDisable(true);
            clearCustomerData();
        }
        
        catch (Exception e)
        {
            databaseErrorLbl.setVisible(true);
            //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    /**
     * newCustomerData is called when new save button (newSaveBtn) is clicked
     * gets new customer data and sends it to the database
     * calling search_customer stored proc first to make sure customer isn't
     * already in database. If resultSet from database returns null,
     * addCustomerNew(editedData) is called and sends array of customer data
     * databaseErrorLbl set to visible if exceptions
     * @throws SQLException
     */
    @FXML
    private void newCustomerData(ActionEvent event) throws SQLException
    {
        turnOffLabels();
        
        String[] editedData = getCustomerEnteredData();
        System.out.println(Arrays.toString(editedData));
        
        String query1 = "{CALL powellcontacts.search_customer(?)}";
        PreparedStatement pstmt = null;
        
        // ** checks if customer is already in database ** //
        try
        {
            Connection manager1 = null;
            manager1 = DriverManager.getConnection(driverManagerString);
            pstmt = manager1.prepareStatement(query1);
            pstmt.setString(1, editedData[0]);
            ResultSet resultSet = pstmt.executeQuery();
            
            while (resultSet.next())
            {
                if (resultSet.getString("customerName") != null)
                {
                    customerEnteredLbl.setVisible(true);
                    newSaveBtn.setDisable(true);
                    disableFields();
                    //break;
                }
                
                else
                {
                    //addCustomerNew(editedData);
                }
            }
            if (!resultSet.next())
            {
                addCustomerNew(editedData);
            }
        }
        
        catch (SQLException e)
        {
            databaseErrorLbl.setVisible(true);
            //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        finally
        {
            if (pstmt != null) { pstmt.close(); }
        }
    }

    /**
     * cancelAction is called when clear button (cancelBtn) is clicked
     * calls turnOffLabels, disables edit and delete buttons
     * and clears data by calling clearCustomerData
     * @throws IOException
     */
    @FXML
    private void cancelAction(ActionEvent event) throws IOException
    {
        turnOffLabels();
        editRecordBtn.setDisable(true);
        deleteRecordBtn.setDisable(true);
        clearCustomerData();
    }

    /**
     * backToMain is called when back to main menu button is clicked
     * sets scene to MainScene with the main menu
     * adds CSS file for style
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
     * getCustomerEnteredData collects data entered into textFields
     * @return customerDataArray[] Strings
     */
    private String[] getCustomerEnteredData()
    {
        String[] customerDataArray = new String[7];
        
        customerDataArray[0] = customerNameField.getText();
        customerDataArray[3] = customerCityField.getText();
        customerDataArray[4] = customerZipField.getText();
        customerDataArray[5] = customerCountry.getText();
        customerDataArray[6] = customerPhoneField.getText();
        customerDataArray[1] = customerAddress.getText();
        customerDataArray[2] = customerAddress2.getText();
        
        return customerDataArray;
    }
    
    /**
     * clearCustomerData clears textFields
     */
    private void clearCustomerData()
    {
        customerNameField.setText("");
        customerAddress.setText("");
        customerAddress2.setText("");
        customerCityField.setText("");
        customerCountry.setText("");
        customerZipField.setText("");
        customerPhoneField.setText("");
    }

    /**
     * diableFields disables the customer textFields
     * except for the customerName field
     */
    private void disableFields()
    {
        customerAddress.setDisable(true);
        customerAddress2.setDisable(true);
        customerCityField.setDisable(true);
        customerCountry.setDisable(true);
        customerZipField.setDisable(true);
        customerPhoneField.setDisable(true);
    }
    
    /**
     * enableFields enables the customer textFields
     * except the customerName field
     */
    private void enableFields()
    {
        customerAddress.setDisable(false);
        customerAddress2.setDisable(false);
        customerCityField.setDisable(false);
        customerCountry.setDisable(false);
        customerZipField.setDisable(false);
        customerPhoneField.setDisable(false);
    }
    
    /**
     * turnOffLabels - setVisible(false) to all 6 of the labels
     *       noRecordsLbl
     *       notActiveLbl
     *       zipcodReqLbl
     *       databaseErrorLbl
     *       customerEnteredLbl
     *       customerSuccessLbl
     */
    private void turnOffLabels()
    {
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        customerSuccessLbl.setVisible(false);
    }
    
    /**
     * addCustomerNew sends editedData array to database by calling
     * new_customer stored procedure
     * customerSuccessLbl if success
     * @param editedData array of customer textFields data
     */
    private void addCustomerNew(String[]editedData)
    {
        String query2 = "{CALL powellcontacts.new_customer(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try
        {
            Connection manager2 = null;
            manager2 = DriverManager.getConnection(driverManagerString);
            PreparedStatement stmt = manager2.prepareStatement(query2);
            stmt.setString(1, editedData[0]);
            stmt.setString(2, editedData[1]);
            stmt.setString(3, editedData[2]);
            stmt.setString(4, editedData[3]);
            stmt.setString(5, editedData[4]);
            stmt.setString(6, editedData[5]);
            stmt.setString(7, editedData[6]);
            stmt.setString(8, user);

            stmt.execute();
            customerSuccessLbl.setVisible(true);
            
            stmt.close();
            manager2.close();
            newSaveBtn.setDisable(true);
            disableFields();
        }

        catch (Exception e)
        {
            databaseErrorLbl.setVisible(true);
        }
    }
    
    /**
     * for validating zip textField when changed FocusListener
     * and for when data is entered using return key
     * uses Regex for zip + 4
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        customerZipField.focusedProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                if (!customerZipField.getText().matches("^\\d{5}(?:[-\\s]\\d{4})?$"))
                {
                    customerZipField.setText("");
                    zipcodeReqLbl.setVisible(true);
                }
            }
        });
        //for validating zip when return is hit
        customerZipField.setOnAction((event) ->
        {
            if (!customerZipField.getText().matches("^\\d{5}(?:[-\\s]\\d{4})?$"))
            {
                customerZipField.setText("");
                zipcodeReqLbl.setVisible(true);
            }
        });
    }
}