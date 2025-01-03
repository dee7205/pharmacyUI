package home;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import DBHandler.*;

public class controller implements Initializable {
    private Stage stage;
    private Scene scene;

    private boolean isSideBarVisible = true;

    @FXML private ImageView exit;
    @FXML private Label Menu;
    @FXML private Label MenuBack;
    @FXML private AnchorPane slider;

    @FXML private Button dashboardButton;
    @FXML private Button itemsButton;
    @FXML private Button pharmacistsButton;
    @FXML private Button restockButton;
    @FXML private Button statisticsButton;

    // dashboard
    @FXML private Label beginningBalance;
    @FXML private Label issuanceBalance;
    @FXML private Label endingBalance;
    @FXML private Label dashboardDate;

    // restock
    @FXML private Button addRestockButton;
    @FXML Button updateRestockButton;
    @FXML private Button deleteRestockButton;

    // pharmacists
    @FXML private Button addPharmacistsButton;
    @FXML private Button updatePharmacistsButton;
    @FXML private Button deletePharmacistsButton;
    @FXML private Button pharmacistTransactHistButton;

    //===================================ITEM METHODS========================================
    @FXML TableView<Item> itemTable;
    @FXML private TableColumn<Item, Double> itemUnitCost_col;
    @FXML private TableColumn<Item, String> itemName_col;
    @FXML private TableColumn<Item, Integer> itemID_col;
    @FXML private TableColumn<Item, Integer> itemQuantity_col;
    @FXML private TableColumn<Item, String> itemUnitType_col;
    @FXML private TableColumn<Item, Double> itemMovement_col;

    public ObservableList<Item> initialItemData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Item [] types = handler.getAllItems(-1);
        return FXCollections.<Item> observableArrayList(types);
    }

    //TODO: Make cells editable
    private void itemEditData(){
//        //Makes the specific columns editable
//        itemTypeNameColumn.setCellFactory(TextFieldTableCell.<ItemType>forTableColumn());
//        itemTypeNameColumn.setOnEditCommit(event ->{
//            ItemType itemType = event.getTableView().getItems().get(event.getTablePosition().getRow());
//            itemType.setItemTypeName(event.getNewValue());
//        });
    }

    // items
    @FXML private Button addItemButton;
    @FXML private Button updateItemButton;
    @FXML private Button deleteItemButton;
    @FXML private Button itemTransactHistButton;

    @FXML private ComboBox<?> item_filterCb;

    // ==============COMBO BOX UNIT TYPE (item.fxml)===================
    @FXML private ComboBox<String> item_unitCb;
    public void unitType_comboBoxOnAction(ActionEvent event) {
        if (item_unitCb != null) {
            String selectAllData = "SELECT * FROM UnitType";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String itemPanel_unit_type = rs.getString("unit_Type");
                        listData.add(itemPanel_unit_type);
                    }

                    item_unitCb.setItems(listData);

                } catch (SQLException e) {
                    System.err.println("SQL Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Database connection failed.");
            }
        } else {
            System.err.println("ComboBox is null");
        }
    }

    // ==============COMBO BOX ITEM TYPE (item.fxml)===================
    @FXML private ComboBox<String> item_TypeCb;

    public void itemType_comboBoxOnAction(ActionEvent event) {
        if (item_TypeCb != null) {
            String selectAllData = "SELECT * FROM ItemType";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String item_type = rs.getString("item_Type");
                        listData.add(item_type);
                    }

                    item_TypeCb.setItems(listData);

                } catch (SQLException e) {
                    System.err.println("SQL Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Database connection failed.");
            }
        } else {
            System.err.println("ComboBox is null");
        }
    }

    // ==============COMBO BOX ITEM NAME (restock.fxml)===================
    @FXML private ChoiceBox<String> restock_item_cb;

    public void restock_itemName_comboBoxOnAction (ActionEvent event) {
        if (restock_item_cb != null) {
            String selectAllData = "SELECT * FROM Items";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String restock_item_name = rs.getString("item_name");
                        listData.add(restock_item_name);
                    }

                    restock_item_cb.setItems(listData);

                } catch (SQLException e) {
                    System.err.println("SQL Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Database connection failed.");
            }
        } else {
            System.err.println("ComboBox is null");
        }
    }

    // ==============COMBO BOX ITEM NAME (transactionWindow.fxml)===================
    @FXML private ComboBox<String> transaction_itemName_comboBox;

    public void transactionItemName_comboBoxOnAction (ActionEvent event) {
        if (transaction_itemName_comboBox != null) {
            String selectAllData = "SELECT * FROM Items";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String transaction_item_name = rs.getString("item_name");
                        listData.add(transaction_item_name);
                    }

                    transaction_itemName_comboBox.setItems(listData);

                } catch (SQLException e) {
                    System.err.println("SQL Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Database connection failed.");
            }
        } else {
            System.err.println("ComboBox is null");
        }
    }

    int index = -1;

    // database tools
    private Connection connect;
    private PreparedStatement pr;
    private ResultSet rs;

    // connecting database
    public Connection connectDB() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/gimatagobrero", "root", "Gimatag2024");
            System.out.println("Connected to database");
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setItem_TypeCb() {
        String selectAllData = "SELECT * FROM items";

        connect = connectDB();

        try {
            pr = connect.prepareStatement(selectAllData);
            rs = pr.executeQuery();

            ObservableList itemList = FXCollections.observableArrayList();

            while (rs.next()) {
                String item = rs.getString("itemTypeID_col");
                itemList.add(item);
            }

            item_TypeCb.setItems(itemList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    //===============================ITEM TYPE METHODS====================================
    @FXML private TableColumn<ItemType, Integer> itemTypeIDColumn;
    @FXML private TableColumn<ItemType, String> itemTypeNameColumn;
    @FXML private TableView<ItemType> itemTypeTable;
    @FXML private TextField itemTypeNameTextField;
    @FXML private Button addItemTypeButton;
    @FXML private Button deleteItemTypeButton;


    public ObservableList<ItemType> initialItemTypeData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        ItemType [] types = handler.getAllItemTypes();
        return FXCollections.<ItemType> observableArrayList(types);
    }

    @FXML
    void addItemType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String itemTypeName = itemTypeNameTextField.getText();
        if (!itemTypeName.isEmpty() && !itemTypeName.equals("Item Type") && handler.addItemType(itemTypeName)) {
            ItemType type = handler.getItemType(itemTypeName);
            itemTypeTable.getItems().add(type); //Adds item type to the table
            itemTypeNameTextField.clear();
        } else if (itemTypeName.equalsIgnoreCase("Item Type")){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Type: " + itemTypeName);
            alert.setContentText("Silly you, you cannot name an Item Type \"Item Type\"");
            alert.showAndWait();
        } else if (!itemTypeName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Type: " + itemTypeName);
            alert.setContentText("Item Type " + itemTypeName + " already exists in the list");
            alert.showAndWait();
        }
    }

    @FXML
    void deleteItemType(ActionEvent event) {
        //Gets the item type selected (Can be null if no item is selected)
        int index = itemTypeTable.getSelectionModel().getSelectedIndex();
        if (index == -1){
            System.out.println("ERROR: Unable to delete Item Type. No index is selected.");
            return;
        }

        ItemType item = itemTypeTable.getSelectionModel().getTableView().getItems().get(index);

        //Error handling
        if (item == null){
            System.out.println("No Item Type Selected.");
            return;
        }

        else {
            SQL_DataHandler handler = new SQL_DataHandler();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Comfirm Item Deletion");
            alert.setHeaderText("Are you sure you want to delete this item type? \nNumber of Affected Items: " + handler.getAffectedItems(item));
            alert.setContentText("Click OK to Delete or Cancel to Discontinue.");

            Optional<ButtonType> result = alert.showAndWait();

            //Removes the list of selected items
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handler.removeItemType(item.getItemTypeName());
                itemTypeTable.getSelectionModel().getTableView().getItems().remove(item);
            }
        }
    }

    //Sets the itemTypeNameColumn edit-ability
    private void itemTypeEditData(){
        itemTypeNameColumn.setEditable(true);
        itemTypeNameColumn.setCellFactory(TextFieldTableCell.<ItemType>forTableColumn());
        itemTypeNameColumn.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();

            ItemType itemType = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (!handler.itemTypeExists(event.getNewValue())){
                handler.updateItemType(itemType.getItemTypeID(), event.getNewValue());
                itemType.setItemTypeName(event.getNewValue());
            }   else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("ERROR");
                    alert.setHeaderText("Unable to update Item Type: " + event.getOldValue() + " to " + event.getNewValue());
                    alert.setContentText("The item name to be updated \"" + event.getNewValue() + "\", already exists in the database.");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        return;
                    }
                }
        });
    }

    //===============================PHARMACIST METHODS====================================

    @FXML private TextField pharmacist_fName_textField;
    @FXML private TextField pharmacist_id_textField;
    @FXML private TextField pharmacist_lName_textField;
    @FXML private TextField pharmacist_mName_textField;

    @FXML private TableView<Pharmacist> pharmacistTable;
    @FXML private TableColumn<Pharmacist, Integer> pharmacistID_col;
    @FXML private TableColumn<Pharmacist, String> pharmacist_fName_col;
    @FXML private TableColumn<Pharmacist, String> pharmacist_mName_col;
    @FXML private TableColumn<Pharmacist, String> pharmacist_lName_col;

    @FXML private Button addPharmacistButton;
    @FXML private Button deletePharmacistButton;

    public ObservableList<Pharmacist> initialPharmacistData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Pharmacist [] pharma = handler.getAllPharmacists();
        return FXCollections.<Pharmacist> observableArrayList(pharma);
    }

    @FXML
    void addPharmacist(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String firstName = pharmacist_fName_textField.getText();
        String middleName = pharmacist_mName_textField.getText();
        String lastName = pharmacist_lName_textField.getText();
        String ID = pharmacist_id_textField.getText();
        int convertedID;
        try {
            convertedID = Integer.parseInt(ID);
        } catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Pharmacist: " + firstName + " " + lastName);
            alert.setContentText("Pharmacist ID " + ID + " is not an Integer.");
            alert.showAndWait();
            return;
        }
        if (!firstName.isEmpty() && !lastName.isEmpty() && !middleName.isEmpty() && !ID.isEmpty() && handler.addPharmacist(convertedID,firstName,middleName,lastName)) {
            Pharmacist p = handler.getPharmacist(convertedID);
            pharmacistTable.getItems().add(p); //Adds item type to the table
            pharmacist_fName_textField.clear();
            pharmacist_mName_textField.clear();
            pharmacist_lName_textField.clear();
            pharmacist_id_textField.clear();

        } else if (!ID.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Pharmacist: " + firstName + " " + lastName);
            alert.setContentText("Pharmacist ID " + ID + " already exists in the list");
            alert.showAndWait();
        }
    }

    private void pharmacistEditData(){
        pharmacist_fName_col.setEditable(true);
        pharmacist_fName_col.setCellFactory(TextFieldTableCell.<Pharmacist>forTableColumn());
        pharmacist_fName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();
            Pharmacist p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            handler.updatePharmacist(p,event.getNewValue(),p.getMiddleName(),p.getLastName());
            p.setFirstName(event.getNewValue());
        });

        pharmacist_mName_col.setEditable(true);
        pharmacist_mName_col.setCellFactory(TextFieldTableCell.<Pharmacist>forTableColumn());
        pharmacist_mName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();
            Pharmacist p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            handler.updatePharmacist(p,p.getFirstName(),event.getNewValue(),p.getLastName());
            p.setMiddleName(event.getNewValue());
        });

        pharmacist_lName_col.setEditable(true);
        pharmacist_lName_col.setCellFactory(TextFieldTableCell.<Pharmacist>forTableColumn());
        pharmacist_lName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();
            Pharmacist p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            handler.updatePharmacist(p,p.getFirstName(),p.getMiddleName(),event.getNewValue());
            p.setLastName(event.getNewValue());
        });
    }

    @FXML
    void deletePharmacist(ActionEvent event) {
        //Gets the pharmacist selected (Can be null if no item is selected)
        int index = pharmacistTable.getSelectionModel().getSelectedIndex();
        if (index == -1){
            System.out.println("ERROR: Unable to delete Pharmacist. No index is selected.");
            return;
        }

        Pharmacist p = pharmacistTable.getSelectionModel().getTableView().getItems().get(index);

        //Error handling
        if (p == null){
            System.out.println("No Pharmacist Selected.");
            return;
        }

        else {
            SQL_DataHandler handler = new SQL_DataHandler();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Pharmacist Deletion");
            alert.setHeaderText("Are you sure you want to delete this Pharmacist? \nNumber of Affected Items: (No Transactions Feature Yet)" );
            alert.setContentText("Click OK to Delete or Cancel to Discontinue.");

            Optional<ButtonType> result = alert.showAndWait();

            //Removes the list of selected items
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handler.removePharmacist(p.getPharmacistID());
                pharmacistTable.getSelectionModel().getTableView().getItems().remove(p);
            }
        }
    }

    //===============================UNIT TYPE METHODS====================================

    @FXML private TableColumn<UnitType, Integer> unitTypeID_col;
    @FXML private TableColumn<UnitType, String> unitTypeName_col;
    @FXML private TableView<UnitType> unitTypeTable;
    @FXML private TextField unitType_textField;
    @FXML private Button addUnitTypeButton;
    @FXML private Button deleteUnitTypeButton;

    public ObservableList<UnitType> initialUnitTypeData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        UnitType [] types = handler.getAllUnitTypes();
        return FXCollections.<UnitType> observableArrayList(types);
    }

    @FXML
    void addUnitType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String unitType = unitType_textField.getText();
        if (!unitType.isEmpty() && handler.addUnitType(unitType)) {
            UnitType type = handler.getUnitType(unitType);
            unitTypeTable.getItems().add(type); //Adds item type to the table
            unitType_textField.clear();

        } else if (!unitType.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Unit Type: " + unitType);
            alert.setContentText("Unit Type " + unitType + " already exists in the list");
            alert.showAndWait();
        }
    }

    private void unitTypeEditData(){
        unitTypeName_col.setEditable(true);
        unitTypeName_col.setCellFactory(TextFieldTableCell.<UnitType>forTableColumn());
        unitTypeName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();

            UnitType unitType = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (!handler.unitTypeExists(event.getNewValue())){
                handler.updateUnitType(unitType.getUnitTypeID(), event.getNewValue());
                unitType.setUnitType(event.getNewValue());
            }   else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to update Unit Type: " + event.getOldValue() + " to " + event.getNewValue());
                alert.setContentText("The Unit Name to be updated \"" + event.getNewValue() + "\", already exists in the database.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    return;
                }
            }
        });
    }

    @FXML
    void deleteUnitType(ActionEvent event) {
        //Gets the unit type selected (Can be null if no item is selected)
        int index = unitTypeTable.getSelectionModel().getSelectedIndex();
        if (index == -1){
            System.out.println("ERROR: Unable to delete Unit Type. No index is selected.");
            return;
        }

        UnitType unit = unitTypeTable.getSelectionModel().getTableView().getItems().get(index);

        //Error handling
        if (unit == null){
            System.out.println("No Unit Type Selected.");
            return;
        }

        else {
            SQL_DataHandler handler = new SQL_DataHandler();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Unit Type Deletion");
            alert.setHeaderText("Are you sure you want to delete this unit type? \nNumber of Affected Items: " + handler.getAffectedItemsForUnit(unit));
            alert.setContentText("Click OK to Delete or Cancel to Discontinue.");

            Optional<ButtonType> result = alert.showAndWait();

            //Removes the list of selected items
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handler.removeUnitType(unit.getUnitType());
                unitTypeTable.getSelectionModel().getTableView().getItems().remove(unit);
            }
        }

    }

    //===============================RESTOCK METHODS====================================

    @FXML private DatePicker expirationDate_datePicker;
    @FXML private DatePicker restockDate_datePicker;

    @FXML private TableColumn<Restocks, Integer> restockID_col;
    @FXML private TableView<Restocks> restockTable;
    @FXML private TableColumn<Restocks, Integer> restock_beginningQty_col;
    @FXML private TableColumn<Restocks, Date> restock_expirationDate_col;
    @FXML private TableColumn<Restocks, String> restock_itemName_col;
    @FXML private TableColumn<Restocks, Date> restock_restockDate_col;
    @FXML private TableColumn<Restocks, Integer> restock_soldQty_col;

    @FXML private TextField restock_qty;

    //===============================TRANSACTION METHODS====================================

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Date> transactionDate_col;
    @FXML private TableColumn<Transaction, Integer> transactionNo_col;
    @FXML private TableColumn<Transaction, Double> transaction_income_col;
    @FXML private TableColumn<Transaction, Integer> transaction_pharmacistID_col;
    @FXML private TableColumn<Transaction, Integer> transaction_soldQty_col;

    @FXML private TableView<ItemsSold> itemsSoldTable;
    @FXML private TableColumn<ItemsSold, Double> itemsSold_income_col;
    @FXML private TableColumn<ItemsSold, String> itemsSold_itemName_col;
    @FXML private TableColumn<ItemsSold, Integer> itemsSold_soldQty_col;
    @FXML private TableColumn<ItemsSold, Double> itemsSold_unitCost_col;


    //===============================ITEM UNIT TYPE METHODS====================================

    @FXML private Button addItemUnitTypeButton;
    @FXML private Button deleteItemUnitTypeButton;
    @FXML private Button updateItemUnitTypeButton;
    @FXML private TextField itemUnitType_unitTypeID_textField;
    @FXML private TextField itemUnitType_itemTypeID_textField;

    @FXML private TableView<ItemUnitType> itemUnitTypeTable;
    @FXML private TableColumn<ItemUnitType, Integer> itemUnitTypeID_col;
    @FXML private TableColumn<ItemUnitType, Integer> itemUnitType_itemTypeID_col;
    @FXML private TableColumn<ItemUnitType, String> itemUnitType_itemTypeName_col;
    @FXML private TableColumn<ItemUnitType, Integer> itemUnitType_unitTypeID_col;
    @FXML private TableColumn<ItemUnitType, String> itemUnitType_unitTypeName_col;

    public ObservableList<ItemUnitType> initialItemUnitTypeData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        ItemUnitType [] types = handler.getAllItemUnitTypes();
        return FXCollections.<ItemUnitType> observableArrayList(types);
    }

    @FXML
    void addItemUnitType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String itemTypeID = itemUnitType_itemTypeID_textField.getText();
        String unitTypeID = itemUnitType_unitTypeID_textField.getText();
        int convertedItemID, convertedUnitID;
        try {
            convertedItemID = Integer.parseInt(itemTypeID);
            convertedUnitID = Integer.parseInt(unitTypeID);
        }
        catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Unit Type.");
            alert.setContentText("Item Type ID/Unit Type ID is not an Integer.");
            alert.showAndWait();
            return;
        }
        if (!itemTypeID.isEmpty() && !unitTypeID.isEmpty() && handler.addItemUnitType(convertedItemID,convertedUnitID)) {

            ItemUnitType type = handler.getItemUnitType(convertedItemID,convertedUnitID);
            itemUnitTypeTable.getItems().add(type); //Adds item type to the table
            itemUnitType_itemTypeID_textField.clear();
            itemUnitType_unitTypeID_textField.clear();

        } else if (!itemTypeID.isEmpty() && !unitTypeID.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Unit Type.");
            alert.setContentText("Combination of Item Type,Unit Type " + convertedItemID + "," + convertedUnitID + " are invalid.");
            alert.showAndWait();
        }
    }

    @FXML
    void deleteItemUnitType(ActionEvent event) {
        //Gets the unit type selected (Can be null if no item is selected)
        int index = itemUnitTypeTable.getSelectionModel().getSelectedIndex();
        if (index == -1){
            System.out.println("ERROR: Unable to delete Item Unit Type. No index is selected.");
            return;
        }

        ItemUnitType unit = itemUnitTypeTable.getSelectionModel().getTableView().getItems().get(index);

        //Error handling
        if (unit == null){
            System.out.println("No Item Unit Type Selected.");
            return;
        }

        else {
            SQL_DataHandler handler = new SQL_DataHandler();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Unit Type Deletion");
            alert.setHeaderText("Are you sure you want to delete this unit type? \nNumber of Affected Items: " + handler.getAffectedItemsForItemUnit(unit));
            alert.setContentText("Click OK to Delete or Cancel to Discontinue.");

            Optional<ButtonType> result = alert.showAndWait();

            //Removes the list of selected items
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handler.removeItemUnitType(unit.getItemUnitTypeID(),9999);
                itemUnitTypeTable.getSelectionModel().getTableView().getItems().remove(unit);
            }
        }

    }


    //===============================STATISTIC METHODS====================================

    @FXML private TableView<Statistics> statisticsTable;
    @FXML private TableColumn<Statistics, ?> statistic_month_col;
    @FXML private TableColumn<Statistics, ?> statistic_day_col;
    @FXML private TableColumn<Statistics, ?> statistic_year_col;
    @FXML private TableColumn<Statistics, Double> statistic_beginningBalance_col;
    @FXML private TableColumn<Statistics, Double> statistic_endingBalance_col;
    @FXML private TableColumn<Statistics, Double> statistic_soldBalance_col;


    //===============================TRANSACTION WINDOW METHODS====================================

    @FXML private Button transactionWindow_addTransactionButton;
    @FXML private Button transactionWindow_removeTransactionButton;
    @FXML private TextField transaction_currentQty_textField;
    @FXML private TextField transaction_sellQty_textField;

    @FXML private TableView<?> transactionWindowTable;
    @FXML private TableColumn<?, ?> transaction_itemName_col;
    @FXML private TableColumn<?, ?> transaction_sellQty_col;
    @FXML private TableColumn<?, ?> transaction_unitCost_col;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (exit != null) {
            exit.setOnMouseClicked(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit Confirmation");
                alert.setHeaderText("Are you sure you want to exit?");
                alert.setContentText("Click OK to exit or Cancel to stay.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    System.exit(0);
                }
            });
        } else {
            System.err.println("Exit ImageView is not initialized properly.");
        }// exit image, close program

        if (slider == null) {
            System.err.println("Slider is not initialized.");
        } else {
            // Ensure the sidebar is open initially
            slider.setTranslateX(0);
            isSideBarVisible = true; // Update the visibility flag
        }

        if (Menu != null && MenuBack != null) {
            Menu.setVisible(false); // hide menu
            MenuBack.setVisible(true); // show menu
        }


        if (Menu != null) {
            Menu.setOnMouseClicked(event -> toggleSideBar());
            MenuBack.setOnMouseClicked(event -> toggleSideBar());
        } else {
            System.out.println("Menu label is not initialized.");
        }

        fetchBalances();

        if (dashboardDate != null) {
            displayDate();
        } else {
            System.out.println("dashboardDate is null");
        }

        //Initialize ITEM TYPE
        if (itemTypeTable != null){
            itemTypeIDColumn.setCellValueFactory(new PropertyValueFactory<ItemType,Integer>("itemTypeID"));
            itemTypeNameColumn.setCellValueFactory(new PropertyValueFactory<ItemType,String>("itemTypeName"));
            itemTypeTable.setItems(initialItemTypeData());
            itemTypeEditData();
        }

        //Initialize UNIT TYPE
        if (unitTypeTable != null){
            unitTypeID_col.setCellValueFactory(new PropertyValueFactory<UnitType,Integer>("unitTypeID"));
            unitTypeName_col.setCellValueFactory(new PropertyValueFactory<UnitType,String>("unitType"));
            unitTypeTable.setItems(initialUnitTypeData());
            unitTypeEditData();
        }

        //Initialize ITEM UNIT TYPE
        if (itemUnitTypeTable != null){
            itemUnitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("itemUnitTypeID"));
            itemUnitType_itemTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("itemTypeID"));
            itemUnitType_unitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("unitTypeID"));
            itemUnitType_itemTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("itemTypeName"));
            itemUnitType_unitTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("unitTypeName"));
            itemUnitTypeTable.setItems(initialItemUnitTypeData());
            //itemUnitTypeEditData();
        }

        //Initialize PHARMACISTS
        if (pharmacistTable != null){
            pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,Integer>("pharmacistID"));
            pharmacist_fName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("firstName"));
            pharmacist_mName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("middleName"));
            pharmacist_lName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("lastName"));

            pharmacistTable.setItems(initialPharmacistData());
            pharmacistEditData();
        }

        //Initialize ITEM
        if (itemTable != null){
            itemID_col.setCellValueFactory(new PropertyValueFactory<Item, Integer>("itemID"));
            itemName_col.setCellValueFactory(new PropertyValueFactory<Item, String>("itemName"));
            itemUnitType_col.setCellValueFactory(new PropertyValueFactory<Item, String>("itemUnitType"));
            itemQuantity_col.setCellValueFactory(new PropertyValueFactory<Item, Integer>("quantity"));
            itemUnitCost_col.setCellValueFactory(new PropertyValueFactory<Item, Double>("unitCost"));
            itemMovement_col.setCellValueFactory(new PropertyValueFactory<Item, Double>("movement"));
            itemTable.setItems(initialItemData());
            itemEditData();
        }

        // combo box from database
        itemType_comboBoxOnAction(new ActionEvent());
        unitType_comboBoxOnAction(new ActionEvent());
        restock_itemName_comboBoxOnAction(new ActionEvent());
        transactionItemName_comboBoxOnAction(new ActionEvent());

        if (unitTypeTable != null){
            //TODO: Add implementation
        }

    }

    // switching scenes
    // Declare variables to track the offset for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    public void switchScene(String fxmlFileName, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();

        // Add dragging functionality to the root node
        root.setOnMousePressed(mouseEvent -> {
                xOffset = mouseEvent.getSceneX();
                yOffset = mouseEvent.getSceneY();
        });

        root.setOnMouseDragged(mouseEvent -> {
            Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            if (currentStage != null) {
                currentStage.setX(mouseEvent.getScreenX() - xOffset);
                currentStage.setY(mouseEvent.getScreenY() - yOffset);
            } else {
                System.err.println("Stage is null. Window dragging won't work.");
            }
        });


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage != null) {
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } else {
            System.err.println("Stage is null. Scene switch failed.");
        }
    }


    public void switchToItems(ActionEvent event) throws IOException {
        switchScene("/home/items.fxml", event);
    }

    public void switchToDashboard(ActionEvent event) throws IOException {
        switchScene("/home/dashboard.fxml", event);
    }

    public void switchToStatistics(ActionEvent event) throws IOException {
        switchScene("/home/statistics.fxml", event);
    }

    public void switchToRestock(ActionEvent event) throws IOException {
        switchScene("/home/restock.fxml", event);
    }

    public void switchToPharmacists(ActionEvent event) throws IOException {
        switchScene("/home/pharmacists.fxml", event);
    }

    public void switchToTransactions(ActionEvent event) throws IOException {
        switchScene("/home/transaction.fxml", event);
    }

    public void switchToItemType(ActionEvent event) throws IOException {
        switchScene("/home/itemType.fxml", event);
    }

    public void switchToUnitType(ActionEvent event) throws IOException {
        switchScene("/home/unitType.fxml", event);
    }

    public void switchToItemUnitType(ActionEvent event) throws IOException {
        switchScene("/home/itemUnitType.fxml", event);
    }

    public void switchToTransactionWindow(ActionEvent event) throws IOException {
        switchScene("/home/transactionWindow.fxml", event);
    }

    public void switchToInputPharmacistIDWindow (ActionEvent event) throws IOException {
        switchScene("/home/inputPharmacistIDWindow.fxml", event);
    }


    // displaying the data nasa dashboard
    public void displayDate() {
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedDate = currentDate.format(formatter);

        dashboardDate.setText(formattedDate);
    }

    public void fetchBalances() {
        double beginning = 150000.00;  // Example value, replace with actual data
        double issuance = 500.00;    // Example value, replace with actual data
        double ending = 12312.73;

        if (beginningBalance != null) {
            beginningBalance.setText(" " + beginning);
        }

        if (issuanceBalance != null) {
            issuanceBalance.setText(" " + issuance);
        }

        if (endingBalance != null) {
            endingBalance.setText("" + ending);
        }

    }

    public void toggleSideBar() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.4));
        slide.setNode(slider);

        // Check if the sidebar is currently visible
        if (isSideBarVisible) {
            slide.setToX(-220); // Slide the sidebar out (close it)
            slide.setOnFinished((ActionEvent e) -> {
                Menu.setVisible(true); // show menu
                MenuBack.setVisible(false); // hide menu
            });
        } else {
            slide.setToX(0); // Slide the sidebar in (open it)
            slide.setOnFinished((ActionEvent e) -> {
                Menu.setVisible(false); // hide menu
                MenuBack.setVisible(true); // show menu
            });
        }

        slide.play();
        isSideBarVisible = !isSideBarVisible; // toggle
    }


}
