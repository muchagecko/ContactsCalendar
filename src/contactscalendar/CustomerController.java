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
 * 
 * @author Elaine Powell <geckoswonderland.com>
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

    @FXML
    private void getCustomerData(ActionEvent event)
    {
        // ** gets data for customer entered from database ** //
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        
        String customerNameEntered = customerNameField.getText();
        Boolean recordFound = false;
        
        Connection manager = null;
        PreparedStatement pstmt = null;

        try
        {
            manager = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/?user=powelle&password=gecko69");
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
                    System.out.println("customerNameEntered = " + customer);
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
            //System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        
        if (recordFound == false)
        {
            noRecordsLbl.setVisible(true);
        }
    }
    
    @FXML
    private void editCustomerData(ActionEvent event)
    {
        // ** sets buttons and fields if customer data is to be edited ** //
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        customerNameToEdit = customerNameField.getText();
        saveRecordBtn.setDisable(false);
        deleteRecordBtn.setDisable(false);
        enableFields();
        editRecordBtn.setDisable(true);
    }
    
    private String[] getCustomerEnteredData()
    {
        // ** gets data entered into fields ** //
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
    
    @FXML
    private void saveCustomerData(ActionEvent event)
    {
        // ** saves data entered to database ** //
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        
        Connection manager = null;
        
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
            manager = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/?user=powelle&password=gecko69");
            PreparedStatement pstmt = manager.prepareStatement("UPDATE powellcontacts.customer AS C LEFT JOIN powellcontacts.address AS A ON C.addressId = A.addressId"
                    + " SET C.customerName = " + "\"" + customerNameEdited + "\"" + ", C.lastUpdateBy = " + "\"" + user + "\"" +  ", A.phone = " + "\"" + customerPhoneEdited + "\"" + ", A.address = " + "\"" + addressEdited + "\"" + ", A.address2 = " + "\"" + address2Edited + "\"" + ", A.lastUpdateBy = " + "\"" + user + "\"" +  ", A.city = " + "\"" + cityEdited + "\"" + ", A.postalCode = " + "\"" + postalCodeEdited + "\"" + ", A.country = " + "\"" + countryEdited + "\""
                    + " WHERE C.customerName = " + "\"" + customerNameToEdit + "\"");            
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
    
    private void disableFields()
    {
        customerAddress.setDisable(true);
        customerAddress2.setDisable(true);
        customerCityField.setDisable(true);
        customerCountry.setDisable(true);
        customerZipField.setDisable(true);
        customerPhoneField.setDisable(true);
    }
    
    private void enableFields()
    {
        customerAddress.setDisable(false);
        customerAddress2.setDisable(false);
        customerCityField.setDisable(false);
        customerCountry.setDisable(false);
        customerZipField.setDisable(false);
        customerPhoneField.setDisable(false);
    }
    
    @FXML
    private void newButtonAction(ActionEvent event)
    {
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        clearCustomerData();
        newSaveBtn.setDisable(false);
        deleteRecordBtn.setDisable(true);
        enableFields();
    }
    
    @FXML
    private void newCustomerData(ActionEvent event)
    {
        // ** gets new customer data and sends it to the database ** //
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        
        String[] editedData = getCustomerEnteredData();
        System.out.println(Arrays.toString(editedData));
        
        String query1 = "{CALL powellcontacts.search_customer(?)}";
        
        // ** checks if customer is already in database ** //
        try
        {
            Connection manager1 = null;
            manager1 = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/?user=powelle&password=gecko69");
            PreparedStatement pstmt = manager1.prepareStatement(query1);
            pstmt.setString(1, editedData[0]);
            ResultSet resultSet = pstmt.executeQuery();
            
            while (resultSet.next())
            {
                if (resultSet.getString("customerName") != null)
                {
                    customerEnteredLbl.setVisible(true);
                    System.out.println("customerName from db: " + resultSet.getString("customerName"));
                    newSaveBtn.setDisable(true);
                    disableFields();
                }
                
                else
                {
                    //addCustomerNew(editedData);
                    System.out.println("add new customer because not already in db");
                }
            }
        }
        
        catch (Exception e)
        {
            databaseErrorLbl.setVisible(true);
            System.out.println("database error from not even");
            //.printException(e);
            //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        finally
        {
            System.out.println("try catch finally!");
            //if (pstmt != null) { pstmt.close(); }
        }
    }
    
    private void addCustomerNew(String[]editedData)
    {
        //String query2 = "{CALL powellcontacts.new_customer(?, ?, ?, ?, ?, ?, ?, ?)}";
        String query2 = "{CALL powellcontacts.new_test(?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try
        {
            Connection manager2 = null;
            manager2 = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/?user=powelle&password=gecko69");
            PreparedStatement stmt = manager2.prepareStatement(query2);
            stmt.setString(1, editedData[5]);
            stmt.setString(2, editedData[4]);
            stmt.setString(3, editedData[3]);
            stmt.setString(4, editedData[2]);
            stmt.setString(5, editedData[1]);
            stmt.setString(6, editedData[0]);
            stmt.setString(7, editedData[6]);
            stmt.setString(8, user);

            stmt.execute();
            System.out.println("Data is successfully inserted into database.");
            stmt.close();
            manager2.close();
            newSaveBtn.setDisable(true);
            disableFields();
        }

        catch (Exception e)
        {
            databaseErrorLbl.setVisible(true);
            System.out.println("database error from NOT having customer already entered");
            //System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
    @FXML
    private void deleteRecord(ActionEvent event) throws IOException
    {
        // ** deletes customer record by inactivating it ** //
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        Connection manager = null;
        String query = "{CALL powellcontacts.inactivate_customer(?, ?)}";
        String customerNameEntered = customerNameField.getText();
                        
        try
        {
            manager = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/?user=powelle&password=gecko69");
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
    
    @FXML
    private void cancelAction(ActionEvent event) throws IOException
    {
        // ** sets error messages off and clears data ** //
        noRecordsLbl.setVisible(false);
        notActiveLbl.setVisible(false);
        zipcodeReqLbl.setVisible(false);
        databaseErrorLbl.setVisible(false);
        customerEnteredLbl.setVisible(false);
        editRecordBtn.setDisable(true);
        deleteRecordBtn.setDisable(true);
        clearCustomerData();
    }
    
    @FXML
    private void backToMain(ActionEvent event) throws IOException
    {
        Parent mainParent = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene MainScene = new Scene(mainParent);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        primaryStage.setScene(MainScene);
        primaryStage.show();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //for validating zip textField when changed FocusListener
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