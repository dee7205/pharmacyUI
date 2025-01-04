package home;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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


    //===================================ITEM METHODS========================================
    @FXML TableView<Item> itemTable;
    @FXML private TableColumn<Item, Double> itemUnitCost_col;
    @FXML private TableColumn<Item, String> itemName_col;
    @FXML private TableColumn<Item, Integer> itemID_col;
    @FXML private TableColumn<Item, Integer> itemQuantity_col;
    @FXML private TableColumn<Item, String> itemUnitType_col;
    @FXML private TableColumn<Item, Double> itemMovement_col;

    @FXML private Button item_AddItemButton;
    @FXML private Button item_DeleteItemButton;
    @FXML private Button item_TransactionHistoryButton;
    @FXML private Button item_UpdateItemButton;

    public ObservableList<Item> initialItemData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Item [] types = handler.getAllItems(-1);
        return FXCollections.<Item> observableArrayList(types);
    }

    private void itemEditData(){
        //Makes the specific columns editable
        itemName_col.setCellFactory(TextFieldTableCell.<Item>forTableColumn());
        itemName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();

            Item item = event.getTableView().getItems().get(event.getTablePosition().getRow());
            if (!handler.itemExists(item.getItemID())){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to update Item Type: " + event.getOldValue() + " to " + event.getNewValue());
                alert.setContentText("Item to be updated doesn't exist: " + item.getItemID());
                alert.showAndWait();
            } else if (handler.itemExists(event.getNewValue())){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to update Item Type: " + event.getOldValue() + " to " + event.getNewValue());
                alert.setContentText("The item name to be updated \"" + event.getNewValue() + "\", already exists in the database.");
                alert.showAndWait();
            }   else {
                handler.updateItem(item.getItemName(), event.getNewValue(), item.getUnitCost());
                item.setItemName(event.getNewValue());
            }
        });
    }

    @FXML private TextField item_filterTextField;
    // ObservableList to hold the data for the table
    private ObservableList<Item> dataList;

    public void search_itemName() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            Item[] search = handler.getAllItems(-1);
            System.out.println("Items fetched: " + Arrays.toString(search)); // debugger

            if (search == null || search.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            itemID_col.setCellValueFactory(new PropertyValueFactory<>("itemID"));
            itemName_col.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            itemUnitType_col.setCellValueFactory(new PropertyValueFactory<>("itemUnitType"));
            itemQuantity_col.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            itemUnitCost_col.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
            itemMovement_col.setCellValueFactory(new PropertyValueFactory<>("movement"));

            // Convert array to ObservableList
            dataList = FXCollections.observableArrayList(search);
            itemTable.setItems(dataList);

            // Add filtering logic
            FilteredList<Item> filteredData = new FilteredList<>(dataList, b -> true);

            item_filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(item -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return String.valueOf(item.getItemID()).contains(lowerCaseFilter) ||
                            item.getItemName().toLowerCase().contains(lowerCaseFilter) ||
                            item.getItemUnitType().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(item.getQuantity()).contains(lowerCaseFilter) ||
                            String.valueOf(item.getUnitCost()).contains(lowerCaseFilter) ||
                            String.valueOf(item.getMovement()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<Item> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(itemTable.comparatorProperty());
            itemTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/gimatagobrero", "root", "shanna05");
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
        return FXCollections.<ItemType> observableArrayList(Arrays.asList(types));
    }

    @FXML
    void addItemType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String itemTypeName = itemTypeNameTextField.getText();
        if (!itemTypeName.isEmpty() && !itemTypeName.equals("Item Type") && handler.addItemType(itemTypeName)) {
            ItemType type = handler.getItemType(itemTypeName);
            ObservableList<ItemType> list = itemTypeTable.getItems(); //Adds item type to the table
            list.add(type);
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

    @FXML private TextField itemType_searchField;
    // ObservableList to hold the data for the table
    ObservableList<ItemType> itemTypeList;

    public void search_itemType() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            ItemType[] types = handler.getAllItemTypes();
            System.out.println("Numbers fetched: " + Arrays.toString(types)); // debugger

            if (types == null || types.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            itemTypeIDColumn.setCellValueFactory(new PropertyValueFactory<ItemType,Integer>("itemTypeID"));
            itemTypeNameColumn.setCellValueFactory(new PropertyValueFactory<ItemType,String>("itemTypeName"));
            itemTypeTable.setItems(initialItemTypeData());

            // Convert array to ObservableList
            itemTypeList = FXCollections.observableArrayList(types);
            itemTypeTable.setItems(itemTypeList);

            // Add filtering logic
            FilteredList<ItemType> filteredData = new FilteredList<>(itemTypeList, b -> true);

            itemType_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(itemType -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return itemType.getItemTypeName().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(itemType.getItemTypeID()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<ItemType> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(itemTypeTable.comparatorProperty());
            itemTypeTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML private Button pharmacist_updatePharmacistButton;
    @FXML private Button pharmacist_TransactionHistoryButton;
    @FXML private Button pharmacist_addPharmacistButton;
    @FXML private Button pharmacist_deletePharmacistButton;

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

        if (firstName.isEmpty() || lastName.isEmpty() || middleName.isEmpty() || ID.isEmpty()){
            return;
        }

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

        if (handler.addPharmacist(convertedID,firstName,middleName,lastName)) {
            refreshPharmacistTable();
            // Pharmacist p = handler.getPharmacist(convertedID);
            // pharmacistTable.getItems().add(p); //Adds Pharmacist to the table
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
            handler.updatePharmacist(p, event.getNewValue(), p.getMiddleName(), p.getLastName());
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
        int index = pharmacistTable.getSelectionModel().getSelectedIndex();

        if (index == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Pharmacist Selected");
            alert.setContentText("Please select a pharmacist to delete.");
            alert.showAndWait();
            return;
        }

        Pharmacist p = pharmacistTable.getSelectionModel().getSelectedItem();

        if (p == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Pharmacist");
            alert.setHeaderText("No Pharmacist Found");
            alert.setContentText("The selected pharmacist is not available.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this Pharmacist?");
        alert.setContentText("Pharmacist ID: " + p.getPharmacistID() + "\nName: " + p.getFirstName() + " " + p.getLastName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SQL_DataHandler handler = new SQL_DataHandler();

            if (handler.removePharmacist(p.getPharmacistID())) {
                refreshPharmacistTable();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Pharmacist Deleted");
                successAlert.setContentText("Pharmacist " + p.getFirstName() + " " + p.getLastName() + " has been successfully deleted.");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Deletion Failed");
                errorAlert.setContentText("Unable to delete the pharmacist from the database.");
                errorAlert.showAndWait();
            }
        }
    }

    // refresh table every action
    private void refreshPharmacistTable() {
        SQL_DataHandler handler = new SQL_DataHandler();

        try {
            // Fetch updated list of pharmacists from the database
            ObservableList<Pharmacist> pharmacistList = FXCollections.observableArrayList(handler.getAllPharmacists());

            // Set the updated list to the table
            pharmacistTable.setItems(pharmacistList);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to Refresh Table");
            alert.setContentText("An error occurred while refreshing the pharmacist table.");
            alert.showAndWait();
        }
    }

    @FXML private TextField pharmacist_searchName;
    // ObservableList to hold the data for the table
    private ObservableList<Pharmacist> pharmaList;

    public void search_pharmacist() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            Pharmacist[] pharmaSearch = handler.getAllPharmacists();
            System.out.println("Numbers fetched: " + Arrays.toString(pharmaSearch)); // debugger

            if (pharmaSearch == null || pharmaSearch.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,Integer>("pharmacistID"));
            pharmacist_fName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("firstName"));
            pharmacist_mName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("middleName"));
            pharmacist_lName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("lastName"));

            // Convert array to ObservableList
            pharmaList = FXCollections.observableArrayList(pharmaSearch);
            pharmacistTable.setItems(pharmaList);

            // Add filtering logic
            FilteredList<Pharmacist> filteredData = new FilteredList<>(pharmaList, b -> true);

            pharmacist_searchName.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(pharmacist -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return pharmacist.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                            pharmacist.getMiddleName().toLowerCase().contains(lowerCaseFilter) ||
                            pharmacist.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(pharmacist.getPharmacistID()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<Pharmacist> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(pharmacistTable.comparatorProperty());
            pharmacistTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
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
        } else {
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

    @FXML private TextField unitType_searchField;
    // ObservableList to hold the data for the table
    ObservableList<UnitType> unitTypeList;

    public void search_unitType() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            UnitType[] unitTypeSearch = handler.getAllUnitTypes();
            System.out.println("Numbers fetched: " + Arrays.toString(unitTypeSearch)); // debugger

            if (unitTypeSearch == null || unitTypeSearch.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            unitTypeID_col.setCellValueFactory(new PropertyValueFactory<UnitType,Integer>("unitTypeID"));
            unitTypeName_col.setCellValueFactory(new PropertyValueFactory<UnitType,String>("unitType"));


            // Convert array to ObservableList
            unitTypeList = FXCollections.observableArrayList(unitTypeSearch);
            unitTypeTable.setItems(unitTypeList);

            // Add filtering logic
            FilteredList<UnitType> filteredData = new FilteredList<>(unitTypeList, b -> true);

            unitType_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(unitType -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return unitType.getUnitType().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(unitType.getUnitTypeID()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<UnitType> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(unitTypeTable.comparatorProperty());
            unitTypeTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //===============================RESTOCK METHODS====================================

    @FXML private DatePicker expirationDate_datePicker;
    @FXML private DatePicker restockDate_datePicker;

    @FXML private TableColumn<Restocks, Integer> restockID_col;
    @FXML private TableView<Restocks> restockTable;
    @FXML private TableColumn<Restocks, Integer> restock_beginningQty_col;
    @FXML private TableColumn<Restocks, String> restock_expirationDate_col;
    @FXML private TableColumn<Restocks, String> restock_itemName_col;
    @FXML private TableColumn<Restocks, String> restock_restockDate_col;
    @FXML private TableColumn<Restocks, Integer> restock_soldQty_col;

    @FXML private TextField restock_qty;

    @FXML private TextField restock_searchTextField;



    /*
    // ObservableList to hold the data for the table
    private ObservableList<Restocks> restockL;

    public void search_restocks() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            // Restocks[] restock = handler.getRestock(2); -> ano lang same ra sa method sa para ma retrieve ang data
            System.out.println("Numbers fetched: " + Arrays.toString(restock)); // debugger

            if (restock == null || restock.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,Integer>("pharmacistID"));
            pharmacist_fName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("firstName"));
            pharmacist_mName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("middleName"));
            pharmacist_lName_col.setCellValueFactory(new PropertyValueFactory<Pharmacist,String>("lastName"));

            // Convert array to ObservableList
            restockList = FXCollections.observableArrayList(pharmaSearch);
            pharmacistTable.setItems(restockList);

            // Add filtering logic
            FilteredList<Restock> filteredData = new FilteredList<>(restockList, b -> true);

            restock_searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(restock -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive) -> change lang sa mga iretrieve everytime mag search ka
                    String lowerCaseFilter = newValue.toLowerCase();
                    return pharmacist.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                            pharmacist.getMiddleName().toLowerCase().contains(lowerCaseFilter) ||
                            pharmacist.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(pharmacist.getPharmacistID()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<Restock> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(restockTable.comparatorProperty());
            restockTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     */


    //===============================TRANSACTION METHODS====================================

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> transactionDate_col;
    @FXML private TableColumn<Transaction, Integer> transactionNo_col;
    @FXML private TableColumn<Transaction, Double> transaction_income_col;
    @FXML private TableColumn<Transaction, Integer> transaction_pharmacistID_col;
    @FXML private TableColumn<Transaction, Integer> transaction_soldQty_col;

    @FXML private TableView<ItemsSold> itemsSoldTable;
    @FXML private TableColumn<ItemsSold, Double> itemsSold_income_col;
    @FXML private TableColumn<ItemsSold, String> itemsSold_itemName_col;
    @FXML private TableColumn<ItemsSold, Integer> itemsSold_soldQty_col;
    @FXML private TableColumn<ItemsSold, Double> itemsSold_unitCost_col;

    public ObservableList<Transaction> initialTransactionData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Transaction [] transactions = handler.getAllTransactions(true);
        return FXCollections.<Transaction> observableArrayList(transactions);
    }

    //Makes it that when a transaction is single or double-clicked, display the items sold
    public void prepareTransactionTableListener(){
        transactionTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1 || mouseEvent.getClickCount() == 2){
                SQL_DataHandler handler = new SQL_DataHandler();
                Transaction transaction = transactionTable.getSelectionModel().getSelectedItem();

                //Checks if the transaction exists in the database
                if (handler.transactionExists(transaction.getTransactionID())){
                    //Initialize the data in the itemsSoldTable
                    itemsSoldTable.setItems(initialItemsSoldData(transaction.getTransactionID()));
                } else {
                    System.out.println("Transaction doesn't exist: " + transaction.getTransactionID());
                }
            }
        });
    }

    public ObservableList<ItemsSold> initialItemsSoldData(int transactionID){
        SQL_DataHandler handler = new SQL_DataHandler();
        ItemsSold [] items = handler.getItemsSold_Transaction(transactionID);
        if (items == null)
            items = new ItemsSold[0];
        return FXCollections.<ItemsSold> observableArrayList(items);
    }

    // searching
    @FXML private TextField transaction_searchField;
    // ObservableList to hold the data for the table
    ObservableList<Transaction> transactionList;

    public void search_transaction() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            Transaction[] transactSearch = handler.getAllTransactions(true);
            System.out.println("Numbers fetched: " + Arrays.toString(transactSearch)); // debugger

            if (transactSearch == null || transactSearch.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            transactionDate_col.setCellValueFactory(new PropertyValueFactory<Transaction, String>("transactionDateString"));
            transactionNo_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("transactionID"));
            transaction_income_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("income"));
            transaction_pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("pharmacistID"));
            transaction_soldQty_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("soldQty"));

            // Convert array to ObservableList
            transactionList = FXCollections.observableArrayList(transactSearch);
            transactionTable.setItems(transactionList);

            // Add filtering logic
            FilteredList<Transaction> filteredData = new FilteredList<>(transactionList, b -> true);

            transaction_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(transaction -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return String.valueOf(transaction.getTransactionDateString()).contains(lowerCaseFilter) ||
                            String.valueOf(transaction.getTransactionID()).contains(lowerCaseFilter) ||
                            String.valueOf(transaction.getIncome()).contains(lowerCaseFilter) ||
                            String.valueOf(transaction.getPharmacistID()).contains(lowerCaseFilter) ||
                            String.valueOf(transaction.getSoldQty()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<Transaction> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(transactionTable.comparatorProperty());
            transactionTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // items sold searching
    @FXML private TextField itemsSold_searchField;
    // ObservableList to hold the data for the table
    ObservableList<ItemsSold> itemSoldList;
    public void search_itemsSold() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            ItemsSold[] itemsSoldSearch = handler.getItemsSold_Item(3);
            System.out.println("Numbers fetched: " + Arrays.toString(itemsSoldSearch)); // debugger

            if (itemsSoldSearch == null || itemsSoldSearch.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            itemsSold_income_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("income"));
            itemsSold_itemName_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, String>("itemName"));
            itemsSold_soldQty_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Integer>("itemQty"));
            itemsSold_unitCost_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("unitCost"));

            // Convert array to ObservableList
            itemSoldList = FXCollections.observableArrayList(itemsSoldSearch);
            itemsSoldTable.setItems(itemSoldList);

            // Add filtering logic
            FilteredList<ItemsSold> filteredData = new FilteredList<>(itemSoldList, b -> true);

            itemsSold_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(itemSold -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return String.valueOf(itemSold.getIncome()).contains(lowerCaseFilter) ||
                            itemSold.getItemName().toLowerCase().contains(lowerCaseFilter) ||
                            String.valueOf(itemSold.getItemQty()).contains(lowerCaseFilter) ||
                            String.valueOf(itemSold.getUnitCost()).contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<ItemsSold> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(itemsSoldTable.comparatorProperty());
            itemsSoldTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



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

        if (!itemTypeID.isEmpty() || !unitTypeID.isEmpty()){
            System.out.println("ERROR: Unable to add Item Unit Type. TextBox is Blank.");
            return;
        }

        int convertedItemID, convertedUnitID;

        try {
            convertedItemID = Integer.parseInt(itemTypeID);
            convertedUnitID = Integer.parseInt(unitTypeID);
        } catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Unit Type.");
            alert.setContentText("Item Type ID/Unit Type ID is not an Integer.");
            alert.showAndWait();
            return;
        }

        if (handler.addItemUnitType(convertedItemID, convertedUnitID)) {
            ItemUnitType type = handler.getItemUnitType(convertedItemID,convertedUnitID);
            itemUnitTypeTable.getItems().add(type); //Adds item type to the table
            itemUnitType_itemTypeID_textField.clear();
            itemUnitType_unitTypeID_textField.clear();

        } else {
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
        } else {
            SQL_DataHandler handler = new SQL_DataHandler();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Unit Type Deletion");
            alert.setHeaderText("Are you sure you want to delete this Item Unit type? \nNumber of Affected Items: " + handler.getAffectedItemsForItemUnit(unit));
            alert.setContentText("Click OK to Delete or Cancel to Discontinue.");

            Optional<ButtonType> result = alert.showAndWait();

            //Removes the list of selected items
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handler.removeItemUnitType(unit.getItemUnitTypeID(), SQL_DataHandler.REMOVE_ITEM_UNIT_TYPE);
                itemUnitTypeTable.getSelectionModel().getTableView().getItems().remove(unit);
            }
        }
    }

    @FXML
    void updateItemUnitType(ActionEvent event){

    }

    // searching
    @FXML private TextField itemUnitType_searchField;
    // ObservableList to hold the data for the table
    ObservableList<ItemUnitType> itemUnitTypeList;

    public void search_ItemUnitType() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            ItemUnitType[] IUTypeSearch = handler.getAllItemUnitTypes();
            System.out.println("Numbers fetched: " + Arrays.toString(IUTypeSearch)); // debugger

            if (IUTypeSearch == null || IUTypeSearch.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            itemUnitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("itemUnitTypeID"));
            itemUnitType_itemTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("itemTypeID"));
            itemUnitType_unitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("unitTypeID"));
            itemUnitType_itemTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("itemTypeName"));
            itemUnitType_unitTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("unitTypeName"));

            // Convert array to ObservableList
            itemUnitTypeList = FXCollections.observableArrayList(IUTypeSearch);
            itemUnitTypeTable.setItems(itemUnitTypeList);

            // Add filtering logic
            FilteredList<ItemUnitType> filteredData = new FilteredList<>(itemUnitTypeList, b -> true);

            itemUnitType_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(itemUnitType -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive)
                    String lowerCaseFilter = newValue.toLowerCase();
                    return String.valueOf(itemUnitType.getItemUnitTypeID()).contains(lowerCaseFilter) ||
                            String.valueOf(itemUnitType.getItemTypeID()).contains(lowerCaseFilter) ||
                            String.valueOf(itemUnitType.getUnitTypeID()).contains(lowerCaseFilter) ||
                            itemUnitType.getItemTypeName().toLowerCase().contains(lowerCaseFilter) ||
                            itemUnitType.getUnitTypeName().toLowerCase().contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<ItemUnitType> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(itemUnitTypeTable.comparatorProperty());
            itemUnitTypeTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
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

        // search_itemType(); (itemType.fxml)
        if (itemType_searchField == null) {
            System.out.println("itemType_searchField is null");
        } else {
            itemType_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_itemType();
        }

        //Initialize UNIT TYPE
        if (unitTypeTable != null){
            unitTypeID_col.setCellValueFactory(new PropertyValueFactory<UnitType,Integer>("unitTypeID"));
            unitTypeName_col.setCellValueFactory(new PropertyValueFactory<UnitType,String>("unitType"));
            unitTypeTable.setItems(initialUnitTypeData());
            unitTypeEditData();
        }

        // search_unitType(); (unitType.fxml)
        if (unitType_searchField == null) {
            System.out.println("unitType_searchField is null");
        } else {
            unitType_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_unitType();
        }

        //Initialize ITEM UNIT TYPE
        if (itemUnitTypeTable != null){
            itemUnitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("itemUnitTypeID"));
            itemUnitType_itemTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("itemTypeID"));
            itemUnitType_unitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,Integer>("unitTypeID"));
            itemUnitType_itemTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("itemTypeName"));
            itemUnitType_unitTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("unitTypeName"));
            itemUnitTypeTable.setItems(initialItemUnitTypeData());
//           itemUnitTypeEditData();
        }

        //  search_ItemUnitType(); (itemUnitType.fxml)
        if (itemUnitType_searchField == null) {
            System.out.println("itemUnitType_searchField is null");
        } else {
            itemUnitType_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_ItemUnitType();
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

        // search_pharmacist();(pharmacists.fxml)
        if (pharmacist_searchName == null) {
            System.out.println("pharmacist_searchName is null");
        } else {
            pharmacist_searchName.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_pharmacist();
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

        //Initialize TRANSACTIONS and ITEMS SOLD
        if (transactionTable != null && itemsSoldTable != null){
            transactionDate_col.setCellValueFactory(new PropertyValueFactory<Transaction, String>("transactionDateString"));
            transactionNo_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("transactionID"));
            transaction_income_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("income"));
            transaction_pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("pharmacistID"));
            transaction_soldQty_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("soldQty"));

            itemsSold_income_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("income"));
            itemsSold_itemName_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, String>("itemName"));
            itemsSold_soldQty_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Integer>("itemQty"));
            itemsSold_unitCost_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("unitCost"));

            prepareTransactionTableListener();
            transactionTable.setItems(initialTransactionData());
        }

        // transaction table
        if (transaction_searchField == null) {
            System.out.println("field is null");
        } else {
            transaction_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_transaction();
        }

        // items sold table
        if (itemsSold_searchField == null) {
            System.out.println("itemsSold_searchField is null");
        } else {
            itemsSold_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_itemsSold();
        }


        // search_itemName(); (items.fxml)
        if (item_filterTextField == null) {
            System.out.println("item_filterTextField is null");
        } else {
            item_filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_itemName();
        }

        // for restock
        /* uncomment if okay na methods
        if (restock_searchTextField == null) {
                    System.out.println("field is null");
                } else {
                    restock_searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                        System.out.println("Search input: " + newValue);
                    });
                    search_restocks();
                }

        if (restockTable != null){
            
            restockTable.setItems(initialRestockData());
        }
        */

        // combo box from database
        itemType_comboBoxOnAction(new ActionEvent());
        unitType_comboBoxOnAction(new ActionEvent());
        restock_itemName_comboBoxOnAction(new ActionEvent());
        transactionItemName_comboBoxOnAction(new ActionEvent());


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
