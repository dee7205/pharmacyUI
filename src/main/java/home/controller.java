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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import DBHandler.*;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

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



    // restock
    @FXML private Button addRestockButton;
    @FXML Button updateRestockButton;
    @FXML private Button deleteRestockButton;


    //===================================ITEM METHODS========================================
    @FXML TableView<Item> itemTable;
    @FXML private TableColumn<Item, Double> items_itemUnitCost_col;
    @FXML private TableColumn<Item, String> items_itemName_col;
    @FXML private TableColumn<Item, Integer> items_itemID_col;
    @FXML private TableColumn<Item, Integer> items_itemQuantity_col;
    @FXML private TableColumn<Item, Integer> items_itemUnitType_col;
    @FXML private TableColumn<Item, Double> items_itemMovement_col;
    @FXML private TableColumn<Item, String> items_itemUnit_col;

    @FXML private TextField itemName_textField;
    @FXML private TextField itemCost_textField;

    @FXML private ComboBox<String> item_TypeCb;
    @FXML private ComboBox<String> item_unitCb;

    @FXML private Button item_AddItemButton;
    @FXML private Button item_DeleteItemButton;
    @FXML private Button item_TransactionHistoryButton;
    @FXML private Button item_UpdateItemButton;

    public ObservableList<Item> initialItemData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Item [] types = handler.getAllItems(-1);
        if (types != null)
            return FXCollections.<Item> observableArrayList(types);
        else
            return FXCollections.<Item> observableArrayList(new ArrayList<>());

    }


    @FXML void addItem(ActionEvent event){
        SQL_DataHandler handler = new SQL_DataHandler();
        String itemName = itemName_textField.getText();
        String itemUnitTypeName = itemUnitType_cb.getSelectionModel().getSelectedItem();
        String itemCost = itemCost_textField.getText();
        int convertedCost;

        try{
            convertedCost = Integer.parseInt(itemCost);
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item.");
            alert.setContentText("Invalid Input. Please Try Again");
            alert.showAndWait();
            return;
        }
        String [] types;
        int itemUnitTypeID;
        try{
            types = itemUnitTypeName.split(",");
            itemUnitTypeID = handler.getItemUnitTypeID(types[0],types[1]);
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item.");
            alert.setContentText("Please Choose an Item Unit Type.");
            alert.showAndWait();
            return;
        }

        System.out.println(types[0] + " " + types[1] + " " + itemUnitTypeName + " = " + itemUnitTypeID);

        if (!itemName.isEmpty() && !itemCost.isEmpty() && handler.addItem(itemName,itemUnitTypeID,convertedCost)) {
            Item i = handler.getItem(itemName);
            itemTable.setItems(initialItemData()); //Adds item type to the table
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ADD ITEM");
            alert.setHeaderText("ADD SUCCESSFUL!");
            alert.setContentText("Item: " + i.getItemName() + " Successfully Added.");
            alert.showAndWait();
            itemName_textField.clear();
            itemUnitType_cb.cancelEdit();
            itemCost_textField.clear();
            search_itemName();

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Type: " + itemName);
            alert.setContentText("Invalid Input. Please Try Again");
            alert.showAndWait();
        }

    }

    @FXML void deleteItem(ActionEvent e){
        //Gets the item type selected (Can be null if no item is selected)
        int index = itemTable.getSelectionModel().getSelectedIndex();
        SQL_DataHandler handler = new SQL_DataHandler();
        if (index == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item.");
            alert.setContentText("Please Choose an Item within the table.");
            alert.showAndWait();
            return;
        }

        Item item = itemTable.getSelectionModel().getTableView().getItems().get(index);
        if (handler.getAffectedRestocks(item) > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item.");
            alert.setContentText("This Item was used in other Tables.");
            alert.showAndWait();
            return;
        }
        //Error handling
        if (item == null){
            System.out.println("No Item Selected.");
            return;
        }

        else {
            handler.removeRestock(item.getItemID(),SQL_DataHandler.REMOVE_BY_ITEM_ID);
            handler.removeItem(item.getItemID());
            itemTable.setItems(initialItemData());
            search_itemName();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DELETE ITEM");
            alert.setHeaderText("DELETE SUCCESSFUL");
            alert.setContentText("Item Successfully Deleted.");
            alert.showAndWait();
        }
    }

    private void itemEditData(){
        //Makes the specific columns editable
        items_itemName_col.setCellFactory(TextFieldTableCell.<Item>forTableColumn());
        items_itemName_col.setOnEditCommit(event ->{
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
                handler.updateItem(item.getItemID(), event.getNewValue(), item.getItemUnitTypeID(), item.getUnitCost());
                item.setItemName(event.getNewValue());
                itemTable.setItems(initialItemData());
                search_itemName();
            }
        });

        items_itemUnitType_col.setEditable(true);
        items_itemUnitType_col.setCellFactory(TextFieldTableCell.<Item,Integer>forTableColumn(new IntegerStringConverter()));
        items_itemUnitType_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            Item item = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(handler.itemUnitTypeExists(e.getNewValue())) {
                handler.updateItem(item.getItemID(), item.getItemName(), e.getNewValue(), item.getUnitCost());
                item.setItemUnitTypeID(e.getNewValue());
                itemTable.setItems(initialItemData());
                search_itemName();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to edit Item Unit Type.");
                alert.setContentText("Item Unit Type ID does not Exist.");
                alert.showAndWait();
            }
        });

        items_itemUnitCost_col.setEditable(true);
        items_itemUnitCost_col.setCellFactory(TextFieldTableCell.<Item,Double>forTableColumn(new DoubleStringConverter()));
        items_itemUnitCost_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            Item item = e.getTableView().getItems().get(e.getTablePosition().getRow());

            handler.updateItem(item.getItemID(), item.getItemName(), item.getItemUnitTypeID(),e.getNewValue());
            item.setUnitCost(e.getNewValue());
            itemTable.setItems(initialItemData());
            search_itemName();
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
            items_itemID_col.setCellValueFactory(new PropertyValueFactory<>("itemID"));
            items_itemName_col.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            items_itemUnitType_col.setCellValueFactory(new PropertyValueFactory<Item, Integer>("itemUnitTypeID"));
            items_itemQuantity_col.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            items_itemUnitCost_col.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
            items_itemMovement_col.setCellValueFactory(new PropertyValueFactory<>("movement"));

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
                    return item.getItemName().toLowerCase().contains(lowerCaseFilter);
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


    // ==============COMBO BOX ITEM NAME (restock.fxml)===================
    @FXML private ChoiceBox<String> restock_item_cb;

    public void restock_itemName_comboBoxOnAction (ActionEvent event) {
        if (restock_item_cb != null) {
            String selectAllData = "SELECT * FROM Items ORDER BY item_name";
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
    @FXML private ComboBox<String> transactionWindow_comboBox;

    public void transactionItemName_comboBoxOnAction (ActionEvent event) {
        if (transactionWindow_comboBox != null) {
            String selectAllData = "SELECT * FROM Items ORDER BY item_name";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String transaction_item_name = rs.getString("item_name");
                        listData.add(transaction_item_name);
                    }

                    transactionWindow_comboBox.setItems(listData);

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

    // ==============COMBO BOX ITEM TYPE (ItemUnitType.fxml)===================
    @FXML private ComboBox<String> itemType_cb;

    public void itemType_cb_onAction (ActionEvent event) {
        if (itemType_cb != null) {
            String selectAllData = "SELECT * FROM ItemType ORDER BY item_type";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String item_type = rs.getString("item_type");
                        listData.add(item_type);
                    }

                    itemType_cb.setItems(listData);

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

    // ==============COMBO BOX UNIT TYPE (ItemUnitType.fxml)===================
    @FXML private ComboBox<String> unitType_cb;

    public void unitType_cb_onAction (ActionEvent event) {
        if (unitType_cb != null) {
            String selectAllData = "SELECT * FROM UnitType ORDER BY unit_type";
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String unit_type = rs.getString("unit_type");
                        listData.add(unit_type);
                    }

                    unitType_cb.setItems(listData);

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

    // ==============COMBO BOX UNIT TYPE (ItemUnitType.fxml)===================
    @FXML private ComboBox<String> itemUnitType_cb;

    public void itemUnitType_cb_onAction (ActionEvent event) {
        if (itemUnitType_cb != null) {
            String selectAllData = """
                    SELECT * FROM ItemUnitType as iut
                    JOIN ItemType as it ON iut.itemType_ID = it.itemType_ID
                    JOIN UnitType as ut ON iut.unitType_ID = ut.unitType_ID
                    GROUP BY iut.item_unit_ID
                    ORDER BY item_type
                    """;
            Connection connect = connectDB(); // Ensure connectDB() is correctly implemented and returns a valid Connection

            if (connect != null) { // Make sure the connection is valid
                try (PreparedStatement pr = connect.prepareStatement(selectAllData);
                     ResultSet rs = pr.executeQuery()) {

                    ObservableList<String> listData = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String item_unit_type = rs.getString("item_type") + "," + rs.getString("unit_type");
                        listData.add(item_unit_type);
                    }

                    itemUnitType_cb.setItems(listData);

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
//            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/gimatagobrero", "root", "Gimatag2024");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/gimatagobrero", "root", "maclang@2023-00570");
            System.out.println("Connected to database");
            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        if (types != null)
            return FXCollections.<ItemType> observableArrayList(types);
        else
            return FXCollections.<ItemType> observableArrayList(new ArrayList<>());
    }

    @FXML
    void addItemType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String itemTypeName = itemTypeNameTextField.getText();
        if (!itemTypeName.isEmpty() && !itemTypeName.equals("Item Type") && handler.addItemType(itemTypeName)) {
            ItemType type = handler.getItemType(itemTypeName);
            System.out.println(type.getItemTypeName()); //Adds item type to the table
            itemTypeTable.setItems(initialItemTypeData());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ADD ITEM TYPE");
            alert.setHeaderText("ADD SUCCESSFUL!");
            alert.setContentText("Item Type: " + itemTypeName + " Successfully Added.");
            alert.showAndWait();
            itemTypeNameTextField.clear();
            search_itemType();


            //REFRESH
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
    void deleteItemType(ActionEvent event) throws SQLException {
        //Gets the item type selected (Can be null if no item is selected)
        int index = itemTypeTable.getSelectionModel().getSelectedIndex();
        SQL_DataHandler handler = new SQL_DataHandler();
        if (index == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item Type.");
            alert.setContentText("Please Choose an Item Type within the table.");
            alert.showAndWait();
            return;
        }
        ItemType item = itemTypeTable.getSelectionModel().getTableView().getItems().get(index);

        if (handler.getAffectedItems(item) > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item Type.");
            alert.setContentText("This Item Type was used in other Tables.");
            alert.showAndWait();
            return;
        }

        //Error handling
        if (item == null){
            System.out.println("No Item Type Selected.");
            return;
        } else {
            handler.removeItemUnitType(item.getItemTypeID(),SQL_DataHandler.REMOVE_ITEM_TYPE);
            handler.removeItemType(item.getItemTypeName());
            itemTypeTable.setItems(initialItemTypeData());
            search_itemType();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DELETE ITEM TYPE");
            alert.setHeaderText("DELETE SUCCESSFUL");
            alert.setContentText("Item Type " + item.getItemTypeName() + " Successfully Deleted.");
            alert.showAndWait();
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
                itemTypeTable.setItems(initialItemTypeData());
                search_itemType();
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
        if (pharma != null)
            return FXCollections.<Pharmacist> observableArrayList(pharma);
        else
            return FXCollections.<Pharmacist> observableArrayList(new ArrayList<>());
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
            alert.setHeaderText("Unable to add new Pharmacist.");
            alert.setContentText("Invalid Input. Please Try Again.");
            alert.showAndWait();
            return;
        }
        if (handler.getPharmacist(convertedID) != null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Pharmacist.");
            alert.setContentText("Pharmacist ID already exists. Please Try Again.");
            alert.showAndWait();
            return;
        }
        if (!firstName.isEmpty() && !lastName.isEmpty() && !middleName.isEmpty() && !ID.isEmpty() && handler.addPharmacist(convertedID,firstName,middleName,lastName)) {
            Pharmacist p = handler.getPharmacist(convertedID);
            pharmacistTable.setItems(initialPharmacistData()); //Adds item type to the table
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ADD PHARMACIST");
            alert.setHeaderText("ADD SUCCESSFUL!");
            alert.setContentText("Pharmacist: " + p.getFirstName() + " " + p.getLastName() + " Successfully Added.");
            alert.showAndWait();
            pharmacist_fName_textField.clear();
            pharmacist_mName_textField.clear();
            pharmacist_lName_textField.clear();
            pharmacist_id_textField.clear();
            search_pharmacist();


        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Pharmacist.");
            alert.setContentText("Invalid Input. Please Try Again.");
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
            pharmacistTable.setItems(initialPharmacistData());
            search_pharmacist();
        });

        pharmacist_mName_col.setEditable(true);
        pharmacist_mName_col.setCellFactory(TextFieldTableCell.<Pharmacist>forTableColumn());
        pharmacist_mName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();
            Pharmacist p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            handler.updatePharmacist(p,p.getFirstName(),event.getNewValue(),p.getLastName());
            p.setMiddleName(event.getNewValue());
            pharmacistTable.setItems(initialPharmacistData());
            search_pharmacist();
        });

        pharmacist_lName_col.setEditable(true);
        pharmacist_lName_col.setCellFactory(TextFieldTableCell.<Pharmacist>forTableColumn());
        pharmacist_lName_col.setOnEditCommit(event ->{
            SQL_DataHandler handler = new SQL_DataHandler();
            Pharmacist p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            handler.updatePharmacist(p,p.getFirstName(),p.getMiddleName(),event.getNewValue());
            p.setLastName(event.getNewValue());
            pharmacistTable.setItems(initialPharmacistData());
            search_pharmacist();
        });
    }

    @FXML
    void deletePharmacist(ActionEvent event) {
        //Gets the pharmacist selected (Can be null if no item is selected)
        int index = pharmacistTable.getSelectionModel().getSelectedIndex();
        SQL_DataHandler handler = new SQL_DataHandler();
        if (index == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Pharmacist.");
            alert.setContentText("Please Choose a Pharmacist within the table.");
            alert.showAndWait();
            return;
        }

        Pharmacist p = pharmacistTable.getSelectionModel().getTableView().getItems().get(index);
        if (handler.getAffectedTransactions(p) > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Pharmacist.");
            alert.setContentText("This Pharmacist was involved in Transactions.");
            alert.showAndWait();
            return;
        }
        //Error handling
        if (p == null){
            System.out.println("No Pharmacist Selected.");
            return;
        }

        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DELETE PHARMACIST");
            alert.setHeaderText("DELETE SUCCESSFUL");
            alert.setContentText("PHARMACIST Successfully Deleted.");
            alert.showAndWait();
            handler.removePharmacist(p.getPharmacistID());
            pharmacistTable.setItems(initialPharmacistData());
            search_pharmacist();

        }
    }

    @FXML private TextField pharmacist_searchName;
    private ObservableList<Pharmacist> pharmaList;

    public void search_pharmacist() {
        try {
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
        if (types != null)
            return FXCollections.<UnitType> observableArrayList(types);
        else
            return FXCollections.<UnitType> observableArrayList(new ArrayList<>());

    }

    @FXML
    void addUnitType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        String unitType = unitType_textField.getText();
        if (!unitType.isEmpty() && handler.addUnitType(unitType)) {
            UnitType type = handler.getUnitType(unitType);
            unitTypeTable.setItems(initialUnitTypeData()); //Adds item type to the table
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ADD UNIT TYPE");
            alert.setHeaderText("ADD SUCCESSFUL!");
            alert.setContentText("Unit Type: " + unitType + " Successfully Added.");
            alert.showAndWait();
            unitType_textField.clear();
            search_unitType();

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
                unitTypeTable.setItems(initialUnitTypeData());
                search_unitType();
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
    void deleteUnitType(ActionEvent event) throws SQLException {
        //Gets the unit type selected (Can be null if no item is selected)
        int index = unitTypeTable.getSelectionModel().getSelectedIndex();
        SQL_DataHandler handler = new SQL_DataHandler();
        if (index == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item Unit Type.");
            alert.setContentText("Please Choose an Item Unit Type within the table.");
            alert.showAndWait();
            return;
        }

        UnitType unit = unitTypeTable.getSelectionModel().getTableView().getItems().get(index);


        if (handler.getAffectedItemsForUnit(unit) > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Unit Type.");
            alert.setContentText("This Unit Type was used in other Tables.");
            alert.showAndWait();
            return;
        }
        //Error handling
        if (unit == null){
            System.out.println("No Unit Type Selected.");
        } else {
            handler.removeItemUnitType(unit.getUnitTypeID(),SQL_DataHandler.REMOVE_UNIT_TYPE);
            handler.removeUnitType(unit.getUnitType());
            unitTypeTable.setItems(initialUnitTypeData());
            search_unitType();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DELETE ITEM TYPE");
            alert.setHeaderText("DELETE SUCCESSFUL");
            alert.setContentText("Item Type " + unit.getUnitType() + " Successfully Deleted.");
            alert.showAndWait();
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
    @FXML private DatePicker restock_fromDatePicker;
    @FXML private DatePicker restock_toDatePicker;
    @FXML private RadioButton restockDateRadioButton;
    @FXML private RadioButton expiryDateRadioButton;

    @FXML private TableColumn<Restocks, Integer> restockID_col;
    @FXML private TableView<Restocks> restockTable;
    @FXML private TableColumn<Restocks, Integer> restock_beginningQty_col;
    @FXML private TableColumn<Restocks, Date> restock_expirationDate_col;
    @FXML private TableColumn<Restocks, Integer> restock_itemID_col;
    @FXML private TableColumn<Restocks, Date> restock_restockDate_col;
    @FXML private TableColumn<Restocks, Integer> restock_soldQty_col;
    @FXML private TableColumn<Restocks, Double>  restock_wholeSaleCost_col;
    @FXML private TableColumn<Restocks, String>  restock_itemName_col;
    @FXML private TextField restock_qty;
    @FXML private TextField wholesale_textField;

    @FXML private TextField restock_searchTextField;
    @FXML private TextField restock_unitCostTextField;


    private ObservableList<Restocks> initialRestockData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Restocks [] r = handler.getAllRestocks();
        if (r != null)
            return FXCollections.<Restocks> observableArrayList(r);
        else
            return FXCollections.<Restocks> observableArrayList(new ArrayList<>());
    }

    private void prepareRestockComboBoxListener(){
        restock_item_cb.setOnAction(event ->{
            String selectedItem = restock_item_cb.getValue();
            if (selectedItem != null && !selectedItem.isEmpty()){
                Item item = new SQL_DataHandler().getItem(selectedItem);

                if (item != null)
                    restock_unitCostTextField.setText(Double.toString(item.getUnitCost()));
                else
                    wholesale_textField.clear();
            }
        });
    }

    @FXML void addRestock(ActionEvent event) throws ParseException {
        SQL_DataHandler handler = new SQL_DataHandler();
        int itemID = handler.getItemId(restock_item_cb.getSelectionModel().getSelectedItem());
        String Qty = restock_qty.getText();
        String restock = restockDate_datePicker.getValue().toString();
        String expiry = expirationDate_datePicker.getValue().toString();
        String wholesale = wholesale_textField.getText();
        if (wholesale_textField == null) {
            System.out.println("wholesale_textField is not initialized!");
        } else {
            System.out.println("wholesale_textField is initialized.");
        }
        System.out.println(restock);

        int convertedQty;
        double convertedCost;
        try {
            convertedQty = Integer.parseInt(Qty);
            convertedCost = Double.parseDouble(wholesale);
        } catch(Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Restock.");
            alert.setContentText("Invalid Input. Please Try Again.");
            alert.showAndWait();
            return;
        }

        Date convertedRestockDate = Date.valueOf(restock);
        Date convertedExpiryDate =  Date.valueOf(expiry);
        if ((!convertedRestockDate.before(convertedExpiryDate) && !convertedRestockDate.equals(convertedExpiryDate))){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Restock");
            alert.setContentText("Restock and Expiry Dates are either Overlapping or Invalid. Please Try Again.");
            alert.showAndWait();
            return;

        };
        if (!restock.toString().isEmpty() && !expiry.toString().isEmpty() && itemID > -1 && handler.addRestock(itemID,convertedQty,convertedCost,restock,expiry)) {
            Restocks r = handler.getLatestRestock();
            restockTable.setItems(initialRestockData());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ADD ITEM");
            alert.setHeaderText("ADD SUCCESSFUL!");
            alert.setContentText("Restock # " + r.getRestockID() + " Successfully Added.");
            alert.showAndWait();
            restock_qty.clear();
            restock_item_cb.getSelectionModel().clearSelection();
            restockDate_datePicker.cancelEdit();
            expirationDate_datePicker.cancelEdit();
            wholesale_textField.clear();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Restock");
            alert.setContentText("Invalid Input. Please Try Again");
            alert.showAndWait();
        }

    }

    @FXML void deleteRestock(ActionEvent e){
        //Gets the restock selected (Can be null if no item is selected)
        int index = restockTable.getSelectionModel().getSelectedIndex();
        if (index == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Restock.");
            alert.setContentText("Please Choose a Restock within the table.");
            alert.showAndWait();
            return;
        }

        Restocks r = restockTable.getSelectionModel().getTableView().getItems().get(index);

        //Error handling
        if (r == null){
            System.out.println("No Restock Selected.");
            return;
        }

        else {
            SQL_DataHandler handler = new SQL_DataHandler();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Item Deletion");
            alert.setHeaderText("Are you sure you want to delete this item type?");
            alert.setContentText("Click OK to Delete or Cancel to Discontinue.");

            Optional<ButtonType> result = alert.showAndWait();
            //Removes the list of selected items
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handler.removeRestock(r.getRestockID(),SQL_DataHandler.REMOVE_BY_RESTOCK_ID);
                restockTable.setItems(initialRestockData());
                search_restocks();
            }
        }

    }

    private void restockEditData(){
        restock_itemID_col.setEditable(true);
        restock_itemID_col.setCellFactory(TextFieldTableCell.<Restocks,Integer>forTableColumn(new IntegerStringConverter()));
        restock_itemID_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            Restocks r = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(handler.getItem(e.getNewValue()) != null) {
                handler.updateRestock(r.getRestockID(), e.getNewValue(),r.getStartQty(),r.getWholesaleCost());
                r.setItemID(e.getNewValue());
                restockTable.setItems(initialRestockData());
                search_restocks();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to Update Restock.");
                alert.setContentText("Item ID does not Exist.");
                alert.showAndWait();
            }
        });

        restock_beginningQty_col.setEditable(true);
        restock_beginningQty_col.setCellFactory(TextFieldTableCell.<Restocks,Integer>forTableColumn(new IntegerStringConverter()));
        restock_beginningQty_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            Restocks r = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(e.getNewValue() > -1 && e.getNewValue() >= r.getSoldQty()) {
                handler.updateRestock(r.getRestockID(),r.getItemID(),e.getNewValue(),r.getWholesaleCost());
                r.setStartQty(e.getNewValue());
                restockTable.setItems(initialRestockData());
                search_itemName();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to Update Restock.");
                alert.setContentText("Invalid Input. Please Try A Positive Integer OR A Number greater than or equal to Sold Quantity.");
                alert.showAndWait();
            }
        });

        restock_wholeSaleCost_col.setEditable(true);
        restock_wholeSaleCost_col.setCellFactory(TextFieldTableCell.<Restocks,Double>forTableColumn(new DoubleStringConverter()));
        restock_wholeSaleCost_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            Restocks r = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(e.getNewValue() > -1) {
                handler.updateRestock(r.getRestockID(),r.getItemID(),r.getStartQty(),e.getNewValue());
                r.setWholesaleCost(e.getNewValue());
                restockTable.setItems(initialRestockData());
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to Update Restock.");
                alert.setContentText("Invalid Input. Please Try A Positive Number.");
                alert.showAndWait();
            }
        });
    }

    @FXML ObservableList<Restocks> restockList;
    public void search_restocks() {
        try {
            // Fetch data from the handler
            SQL_DataHandler handler = new SQL_DataHandler();
            Restocks[] restock = handler.getAllRestocks(); //-> ano lang same ra sa method sa para ma retrieve ang data
            System.out.println("Numbers fetched: " + Arrays.toString(restock)); // debugger

            if (restock == null || restock.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns
            restockID_col.setCellValueFactory(new PropertyValueFactory<Restocks, Integer>("restockID"));
            restock_itemID_col.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("itemID"));
            restock_beginningQty_col.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("startQty"));
            restock_soldQty_col.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("soldQty"));
            restock_restockDate_col.setCellValueFactory(new PropertyValueFactory<Restocks,Date>("restockDate"));
            restock_expirationDate_col.setCellValueFactory(new PropertyValueFactory<Restocks,Date>("expiryDate"));
            restock_wholeSaleCost_col.setCellValueFactory(new PropertyValueFactory<Restocks,Double>("wholesaleCost"));

            // Convert array to ObservableList
            restockList = FXCollections.observableArrayList(restock);
            restockTable.setItems(restockList);

            // Add filtering logic
            FilteredList<Restocks> filteredData = new FilteredList<>(restockList, b -> true);

            restock_searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(restocks -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Filter items by name (case-insensitive) -> change lang sa mga iretrieve everytime mag search ka
                    String lowerCaseFilter = newValue.toLowerCase();
                    return String.valueOf(restocks.getRestockID()).contains(lowerCaseFilter) ||
                            restocks.getRestockDate().contains(lowerCaseFilter);
                });
            });

            // Bind the sorted data to the table
            SortedList<Restocks> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(restockTable.comparatorProperty());
            restockTable.setItems(sortedData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // filtering date
    void fetchOriginalTable_Restocks() {
        ObservableList<Restocks> originalRestockList = FXCollections.observableArrayList(restockList);
        restockTable.setItems(originalRestockList);
    }
    @FXML
    void restock_filterButtonOnClick(ActionEvent event) {

        if (restock_fromDatePicker == null || restock_toDatePicker == null) {
            System.out.print("DatePicker controls are not properly initialized.");
            fetchOriginalTable_Restocks();
            return;
        }

        // Get selected dates from the DatePickers
        LocalDate fromDate = restock_fromDatePicker.getValue();
        LocalDate toDate = restock_toDatePicker.getValue();

        if (fromDate == null || toDate == null) {
            fetchOriginalTable_Restocks();
            return;
        }

        if (fromDate.isAfter(toDate)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Date Range");
            alert.setHeaderText("The 'From' date cannot be after the 'To' date.");
            alert.showAndWait();
            return;
        }

        boolean isRestockDateFilter = restockDateRadioButton.isSelected();
        boolean isExpiryDateFilter = expiryDateRadioButton.isSelected();

        List<Restocks> filteredItems = restockList.stream()
                .filter(item -> {
                    LocalDate dateToCompare = null;

                    if (isRestockDateFilter) {
                        dateToCompare = LocalDate.parse(item.getRestockDate());
                    } else if (isExpiryDateFilter) {
                        dateToCompare = LocalDate.parse(item.getExpiryDate());
                    } else {
                        return false;
                    }

                    return !dateToCompare.isBefore(fromDate) && !dateToCompare.isAfter(toDate);
                })
                .collect(Collectors.toList());




        // Convert filtered items to ObservableList
        ObservableList<Restocks> observableFilteredItems = FXCollections.observableArrayList(filteredItems);

        // If there are no filtered items, show a warning and reset to original list
        if (filteredItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Results");
            alert.setHeaderText("No items match the filter criteria.");
            alert.showAndWait();
            fetchOriginalTable_Restocks();
        } else {
            // Set the filtered data in the TableView
            restockTable.setItems(observableFilteredItems);
        }

    }


    //===============================TRANSACTION METHODS====================================

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> transactionDate_col;
    @FXML private TableColumn<Transaction, Integer> transactionNo_col;
    @FXML private TableColumn<Transaction, Double> transaction_income_col;
    @FXML private TableColumn<Transaction, Integer> transaction_pharmacistID_col;
    @FXML private TableColumn<Transaction, Integer> transaction_soldQty_col;
    @FXML private TableColumn<Transaction, String> transaction_pharmacistName_col;

    @FXML private TableView<ItemsSold> itemsSoldTable;
    @FXML private TableColumn<ItemsSold, Double> itemsSold_income_col;
    @FXML private TableColumn<ItemsSold, String> itemsSold_itemName_col;
    @FXML private TableColumn<ItemsSold, Integer> itemsSold_soldQty_col;
    @FXML private TableColumn<ItemsSold, Double> itemsSold_unitCost_col;

    @FXML private RadioButton itemName_radioboxButton;
    @FXML private RadioButton pharmacistID_radioboxButton;
    @FXML private RadioButton transactionID_radioboxButton;

    @FXML private TextField transaction_searchField;
    @FXML private DatePicker transaction_fromDatePicker;
    @FXML private DatePicker transaction_toDatePicker;

    public ObservableList<Transaction> initialTransactionData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        Transaction [] transactions = handler.getAllTransactions(true);
        return FXCollections.<Transaction> observableArrayList(transactions);
    }

    //Makes it that when a transaction is single or double clicked, display the items sold
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

        if (items != null)
            return FXCollections.<ItemsSold> observableArrayList(items);
        else
            return  FXCollections.<ItemsSold> observableArrayList(new ArrayList<>());
    }


    @FXML
    ObservableList<Transaction> transactionList;
    @FXML
    ObservableList<ItemsSold> itemsSoldList;

    public void search_transaction() {
        try {
            SQL_DataHandler handler = new SQL_DataHandler();

            // Fetch transactions and items sold data
            Transaction[] transactions = handler.getAllTransactions(true);
//            ItemsSold[] itemsSold = handler.get(true);

            System.out.println("Numbers fetched: " + Arrays.toString(transactions)); // Debugger

            if (transactions == null || transactions.length == 0) {
                System.out.println("No data retrieved from the database.");
                return;
            }

            // Set up table columns for the transactions
            transactionDate_col.setCellValueFactory(new PropertyValueFactory<Transaction, String>("transactionDate"));
            transactionNo_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("transactionID"));
            transaction_income_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("income"));
            transaction_pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("pharmacistID"));
            transaction_soldQty_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("soldQty"));

            // Set up table columns for items sold
            itemsSold_income_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("income"));
            itemsSold_itemName_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, String>("itemName"));
            itemsSold_soldQty_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Integer>("itemQty"));
            itemsSold_unitCost_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("unitCost"));

            // Convert arrays to ObservableLists
            transactionList = FXCollections.observableArrayList(transactions);
//            itemsSoldList = FXCollections.observableArrayList(itemsSold);

            // Set the tables' items
            transactionTable.setItems(transactionList);
            itemsSoldTable.setItems(itemsSoldList);

            // Add filtering logic
            FilteredList<Transaction> filteredTransactionData = new FilteredList<>(transactionList, b -> true);
            // FilteredList<ItemsSold> filteredItemsSoldData = new FilteredList<>(itemsSoldList, b -> true);

            // Filter based on the search field
            transaction_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredTransactionData.setPredicate(transaction -> {
                    // If search field is empty, show all items
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();

                    // Filter for transactions based on selected radio button
                    if (pharmacistID_radioboxButton.isSelected()) {
                        return String.valueOf(transaction.getPharmacistID()).contains(lowerCaseFilter);
                    } else if (transactionID_radioboxButton.isSelected()) {
                        return String.valueOf(transaction.getTransactionID()).contains(lowerCaseFilter);
                    }
//                   } else if (itemName_radioboxButton.isSelected()) { // Use itemName here instead of itemID
//                        return transaction.getItemName().toLowerCase().contains(lowerCaseFilter);
//                    }

                    return false; // No match found
                });

//                // Filter for items sold based on item name
//                filteredItemsSoldData.setPredicate(itemSold -> {
//                    if (newValue == null || newValue.isEmpty()) {
//                        return true;
//                    }
//                    return itemSold.getItemName().toLowerCase().contains(newValue.toLowerCase());
//                });
            });

            // Bind the sorted data to the transaction table
            SortedList<Transaction> sortedTransactionData = new SortedList<>(filteredTransactionData);
            sortedTransactionData.comparatorProperty().bind(transactionTable.comparatorProperty());
            transactionTable.setItems(sortedTransactionData);

            // Bind the sorted data to the items sold table
//            SortedList<ItemsSold> sortedItemsSoldData = new SortedList<>(filteredItemsSoldData);
            //sortedItemsSoldData.comparatorProperty().bind(itemsSoldTable.comparatorProperty());
//            itemsSoldTable.setItems(sortedItemsSoldData);

            System.out.println("Search setup complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // radiobutton
    private void enableSearchFieldIfRadioSelected() {
        if (pharmacistID_radioboxButton.isSelected() || transactionID_radioboxButton.isSelected()) {
            transaction_searchField.setDisable(false);
        } else {
            transaction_searchField.setDisable(true);
        }
    }

    // filtering date
    void fetchOriginalTable_Transaction() {
        ObservableList<Transaction> originalTransactionList = FXCollections.observableArrayList(transactionList);
        transactionTable.setItems(originalTransactionList);
    }

    void initializeTransactionList() {
        SQL_DataHandler handler = new SQL_DataHandler();
        Transaction[] transactionArray = handler.getAllTransactions(true);

        transactionList = FXCollections.observableArrayList(
                Arrays.stream(transactionArray)
                        .filter(transaction -> transaction.getTransactionDate() != null)
                        .collect(Collectors.toList())
        );
        transactionTable.setItems(transactionList);
    }

    @FXML
    void transaction_filterButtonOnClick(ActionEvent event) {
        // Ensure fromDate and toDate are properly initialized
        if (transaction_fromDatePicker == null || transaction_toDatePicker == null) {
            System.out.println("DatePickers are not properly initialized.");
            return;
        }

        LocalDate fromDate = transaction_fromDatePicker.getValue();
        LocalDate toDate = transaction_toDatePicker.getValue();

        if (fromDate == null || toDate == null) {
            System.out.println("No date range selected. Showing all transactions.");
            transactionTable.setItems(transactionList); // Show all transactions
            return;
        }

        if (fromDate.isAfter(toDate)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Date Range");
            alert.setHeaderText("The 'From' date cannot be after the 'To' date.");
            alert.showAndWait();
            return;
        }

        // Perform filtering
        List<Transaction> filteredItems = transactionList.stream()
                .filter(transaction -> {
                    LocalDate dateToCompare = transaction.getTransactionDate().toLocalDate();
                    return !dateToCompare.isBefore(fromDate) && !dateToCompare.isAfter(toDate);
                })
                .collect(Collectors.toList());

        ObservableList<Transaction> observableFilteredItems = FXCollections.observableArrayList(filteredItems);
        transactionTable.setItems(observableFilteredItems);

        if (filteredItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Results");
            alert.setHeaderText("No items match the filter criteria.");
            alert.showAndWait();
        }
    }


    //===============================ITEM UNIT TYPE METHODS====================================

    @FXML private Button addItemUnitTypeButton;
    @FXML private Button deleteItemUnitTypeButton;
    @FXML private Button updateItemUnitTypeButton;


    @FXML private TableView<ItemUnitType> itemUnitTypeTable;
    @FXML private TableColumn<ItemUnitType, Integer> itemUnitTypeID_col;
    @FXML private TableColumn<ItemUnitType, Integer> itemUnitType_itemTypeID_col;
    @FXML private TableColumn<ItemUnitType, String> itemUnitType_itemTypeName_col;
    @FXML private TableColumn<ItemUnitType, Integer> itemUnitType_unitTypeID_col;
    @FXML private TableColumn<ItemUnitType, String> itemUnitType_unitTypeName_col;

    public ObservableList<ItemUnitType> initialItemUnitTypeData(){
        SQL_DataHandler handler = new SQL_DataHandler();
        ItemUnitType [] types = handler.getAllItemUnitTypes();
        if (types != null)
            return FXCollections.<ItemUnitType> observableArrayList(types);
        else
            return FXCollections.<ItemUnitType> observableArrayList(new ArrayList<>());
    }

    @FXML
    void addItemUnitType(ActionEvent event) { //ADD ITEM TYPE
        SQL_DataHandler handler = new SQL_DataHandler();
        int itemTypeID = -1, unitTypeID = -1;
        itemTypeID = handler.getItemTypeID(itemType_cb.getSelectionModel().getSelectedItem());
        unitTypeID = handler.getUnitTypeID(unitType_cb.getSelectionModel().getSelectedItem());

        if (itemTypeID == -1 || unitTypeID == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Add Item Unit Type.");
            alert.setContentText("Please Choose an Item Type ID/Unit Type ID.");
            alert.showAndWait();
            return;
        }

        handler.addItemUnitType(itemTypeID,unitTypeID);
        ItemUnitType type = handler.getItemUnitType(itemTypeID,unitTypeID);
        itemUnitTypeTable.setItems(initialItemUnitTypeData()); //Adds item type to the table
        itemType_cb.cancelEdit();
        unitType_cb.cancelEdit();
        search_ItemUnitType();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ADD ITEM UNIT TYPE");
        alert.setHeaderText("ADD SUCCESSFUL!");
        alert.setContentText("Item Unit Type: " + type.getItemTypeName() + "-" + type.getUnitTypeName() +" Successfully Added.");
        alert.showAndWait();

    }

    @FXML
    void deleteItemUnitType(ActionEvent event) throws SQLException {
        //Gets the unit type selected (Can be null if no item is selected)
        int index = itemUnitTypeTable.getSelectionModel().getSelectedIndex();
        SQL_DataHandler handler = new SQL_DataHandler();
        if (index == -1){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item Unit Type.");
            alert.setContentText("Please Choose an Item Unit Type within the table.");
            alert.showAndWait();
            return;
        }
        ItemUnitType unit = itemUnitTypeTable.getSelectionModel().getTableView().getItems().get(index);

        if (handler.getAffectedItemsForItemUnit(unit) > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to Delete Item Unit Type.");
            alert.setContentText("This Item Unit Type was used in other Tables.");
            alert.showAndWait();
            return;
        }
        //Error handling
        if (unit == null){
            System.out.println("No Item Unit Type Selected.");
        } else {
            handler.removeItemUnitType(unit.getItemUnitTypeID(), SQL_DataHandler.REMOVE_ITEM_UNIT_TYPE);
            itemUnitTypeTable.setItems(initialItemUnitTypeData());
            search_ItemUnitType();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DELETE ITEM UNIT TYPE");
            alert.setHeaderText("DELETE SUCCESSFUL");
            alert.setContentText("Item Unit Type Successfully Deleted.");
            alert.showAndWait();
        }
    }

    @FXML
    void itemUnitTypeEditData(){
        itemUnitType_itemTypeID_col.setEditable(true);
        itemUnitType_itemTypeID_col.setCellFactory(TextFieldTableCell.<ItemUnitType,Integer>forTableColumn(new IntegerStringConverter()));
        itemUnitType_itemTypeID_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            ItemUnitType p = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(handler.getItemType(e.getNewValue()) != null) {
                handler.updateItemUnitType(p.getItemUnitTypeID(), e.getNewValue(), p.getUnitTypeID());
                p.setItemTypeID(e.getNewValue());
                itemUnitTypeTable.setItems(initialItemUnitTypeData());
                search_ItemUnitType();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to edit Item Unit Type.");
                alert.setContentText("Item Type ID/Unit Type ID does not Exist.");
                alert.showAndWait();
            }
        });

        itemUnitType_unitTypeID_col.setEditable(true);
        itemUnitType_unitTypeID_col.setCellFactory(TextFieldTableCell.<ItemUnitType,Integer>forTableColumn(new IntegerStringConverter()));
        itemUnitType_unitTypeID_col.setOnEditCommit(e -> {
            SQL_DataHandler handler = new SQL_DataHandler();
            ItemUnitType p = e.getTableView().getItems().get(e.getTablePosition().getRow());
            if(handler.getUnitType(e.getNewValue()) != null) {
                handler.updateItemUnitType(p.getItemUnitTypeID(),p.getItemTypeID(),e.getNewValue());
                p.setUnitTypeID(e.getNewValue());
                itemUnitTypeTable.setItems(initialItemUnitTypeData());
                search_ItemUnitType();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to edit Item Unit Type.");
                alert.setContentText("Item Type ID/Unit Type ID does not Exist.");
                alert.showAndWait();
            }
        });
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
            itemUnitType_itemTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType, Integer>("itemTypeID"));
            itemUnitType_unitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType, Integer>("unitTypeID"));
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
    @FXML private Button confirmTransaction_button;
    @FXML private TextField transactionWindow_currentQty_textfield;
    @FXML private TextField transactionWindow_sellQty_textField;
    @FXML private Text transactionWindow_PharmacistName;
    @FXML private Text transactionWindow_Income;

    @FXML private TableView<TransactionWindow> transactionWindowTable;
    @FXML private TableColumn<TransactionWindow, String> transactionWindow_itemName_col;
    @FXML private TableColumn<TransactionWindow, Integer> transactionWindow_sellQty_col;
    @FXML private TableColumn<TransactionWindow, Double> transactionWindow_unitCost_col;

    public List<TransactionWindow> transactionWindowList;

    public void prepareTransactionWindowComboBox(){
        Item [] items = new SQL_DataHandler().getAllItems(-1);
        ObservableList<String> listData = FXCollections.observableArrayList();

        for (Item item : items)
            listData.add(item.getItemName());
        transactionWindow_comboBox.setItems(listData);

        transactionWindow_comboBox.setOnAction(event ->{
            String selectedItem = transactionWindow_comboBox.getValue();
            if (selectedItem != null && !selectedItem.isEmpty()){
                SQL_DataHandler handler = new SQL_DataHandler();
                int itemAmount = handler.getRemainingStockQuantity(handler.getItemId(selectedItem));

                if (itemAmount > -1)
                    transactionWindow_currentQty_textfield.setText(Integer.toString(itemAmount));
                else
                    transactionWindow_currentQty_textfield.clear();
            } else
                transactionWindow_currentQty_textfield.clear();
        });
    }

    //Updates the Total Income text
    public void prepareTransactionWindowIncome(){
        if (transactionWindowList.isEmpty()){
            transactionWindow_Income.setText("N/A");
        } else {
            SQL_DataHandler handler = new SQL_DataHandler();
            double totalPrice = 0;
            for (TransactionWindow trans : transactionWindowList)
                totalPrice += handler.getItem(trans.getItemName()).getUnitCost() * trans.getSellQty();
            transactionWindow_Income.setText(new DecimalFormat().format(totalPrice));
        }
    }

    public ObservableList<TransactionWindow> initialTransactionWindowData(){
        TransactionWindow [] array = new TransactionWindow[transactionWindowList.size()];
        return FXCollections.<TransactionWindow> observableArrayList(transactionWindowList.toArray(array));
    }

    @FXML
    public void addTransactionWindow(){
        if (transactionWindowList == null){
            transactionWindowList = new ArrayList<>();
        }

        String itemName = transactionWindow_comboBox.getValue();
        String currentQty = transactionWindow_currentQty_textfield.getText();
        String soldQty = transactionWindow_sellQty_textField.getText();

        if (itemName.isEmpty() || currentQty.isEmpty() || soldQty.isEmpty()){
            System.out.println("Textbox or Combo box are empty");
            return;
        }

        int soldQtyInt;
        try {
            soldQtyInt = Integer.parseInt(soldQty);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        SQL_DataHandler handler = new SQL_DataHandler();
        Item item = handler.getItem(itemName);
        int remainingStock = handler.getRemainingStockQuantity(item.getItemID());

        TransactionWindow tw = new TransactionWindow(item.getItemName(), soldQtyInt, item.getUnitCost());
        int itemQuantity = 0;

        for (TransactionWindow trans : transactionWindowList){
            if (trans.getItemName().equals(tw.getItemName()))
                itemQuantity += trans.getSellQty();

            if (itemQuantity > remainingStock) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText("Unable to add new Item Sold.");
                alert.setContentText("Overall quantity exceeds current quantity.");
                alert.showAndWait();
                return;
            }
        }

        if (soldQtyInt + itemQuantity <= remainingStock){
            transactionWindowList.add(tw);
            transactionWindowTable.setItems(initialTransactionWindowData());
        }   else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("Unable to add new Item Sold.");
            alert.setContentText("Overall quantity exceeds current quantity.");
            alert.showAndWait();
            return;
        }

        prepareTransactionWindowIncome();
    }

    @FXML
    public void deleteTransactionWindow(){
        TransactionWindow tw = transactionWindowTable.getSelectionModel().getSelectedItem();

        if (tw == null){
            System.out.println("No transaction selected");
            return;
        }

        for (int i  = 0; i < transactionWindowList.size(); i++){
            if (transactionWindowList.get(i).getItemName().equals(tw.getItemName())){
                transactionWindowList.remove(i);
                transactionWindowTable.setItems(initialTransactionWindowData());
                prepareTransactionWindowIncome();
                return;
            }
        }

        System.out.println("No transaction was deleted.");
    }

    @FXML
    public void confirmTransaction(){
        if (transactionWindowList.isEmpty())
            return;

        SQL_DataHandler handler = new SQL_DataHandler();
        handler.addTransaction(pharmacistID);
        int transactionID = handler.getLatestTransactionID();

        System.out.println(transactionID + "-" + pharmacistID + "-" + transactionWindowList.size());

        for (TransactionWindow trans : transactionWindowList){
            int itemID = handler.getItemId(trans.getItemName());
            handler.addItemsSold(transactionID, itemID, trans.getSellQty());
        }

        transactionWindowList = new ArrayList<>();
        transactionWindowTable.setItems(initialTransactionWindowData());
    }


    //===============================INPUT PHARMACIST ID WINDOW METHODS====================================
    @FXML private Button inputPharmaID_nextButton;
    @FXML private TextField input_pharmacistID_textfield;

    public static int pharmacistID = 0;

    // check if its in the database
    private boolean isPharmacistIdValid(String pharmacistIDString) {
        SQL_DataHandler handler = new SQL_DataHandler();
        try{
            int pharmaID = Integer.parseInt(pharmacistIDString);
            pharmacistID = pharmaID;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return handler.checkPharmacistIDInDatabase(pharmacistIDString);
    }

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


        //Initialize Dashboard
        if (dashboardDate != null) {

            loadDashboardData();
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
            itemUnitType_itemTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType, Integer>("itemTypeID"));
            itemUnitType_unitTypeID_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType, Integer>("unitTypeID"));
            itemUnitType_itemTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("itemTypeName"));
            itemUnitType_unitTypeName_col.setCellValueFactory(new PropertyValueFactory<ItemUnitType,String>("unitTypeName"));
            itemUnitTypeTable.setItems(initialItemUnitTypeData());
            itemUnitTypeEditData();
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

        //Initialize ITEMS
        if (itemTable != null){
            items_itemID_col.setCellValueFactory(new PropertyValueFactory<Item, Integer>("itemID"));
            items_itemName_col.setCellValueFactory(new PropertyValueFactory<Item, String>("itemName"));
            items_itemUnitType_col.setCellValueFactory(new PropertyValueFactory<Item, Integer>("itemUnitTypeID"));
            items_itemQuantity_col.setCellValueFactory(new PropertyValueFactory<Item, Integer>("quantity"));
            items_itemUnitCost_col.setCellValueFactory(new PropertyValueFactory<Item, Double>("unitCost"));
            items_itemMovement_col.setCellValueFactory(new PropertyValueFactory<Item, Double>("movement"));
            items_itemUnit_col.setCellValueFactory(new PropertyValueFactory<Item, String>("itemUnitType"));
            itemTable.setItems(initialItemData());
            itemEditData();
        }


        //Initialize RESTOCK
        if (restockTable != null){
            restockID_col.setCellValueFactory(new PropertyValueFactory<Restocks, Integer>("restockID"));
            restock_itemID_col.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("itemID"));
            restock_beginningQty_col.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("startQty"));
            restock_soldQty_col.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("soldQty"));
            restock_restockDate_col.setCellValueFactory(new PropertyValueFactory<Restocks,Date>("restockDate"));
            restock_expirationDate_col.setCellValueFactory(new PropertyValueFactory<Restocks,Date>("expiryDate"));
            restock_wholeSaleCost_col.setCellValueFactory(new PropertyValueFactory<Restocks,Double>("wholesaleCost"));
            restock_itemName_col.setCellValueFactory(new PropertyValueFactory<Restocks,String>("itemName"));

            restockDate_datePicker.setValue(new SQL_DataHandler().getCurrentDate());
            restockTable.setItems(initialRestockData());
            prepareRestockComboBoxListener();
            restockEditData();
        }

        //Initialize TRANSACTION WINDOW
        if (transactionWindowTable != null){
            transactionWindow_itemName_col.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            transactionWindow_sellQty_col.setCellValueFactory(new PropertyValueFactory<>("sellQty"));
            transactionWindow_unitCost_col.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
            transactionWindowList = new ArrayList<>();

            transactionWindowTable.setItems(initialTransactionWindowData());
        }

        //Initialize TRANSACTIONS and ITEMS SOLD
        if (transactionTable != null && itemsSoldTable != null){
            transactionDate_col.setCellValueFactory(new PropertyValueFactory<Transaction, String>("transactionDateString"));
            transactionNo_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("transactionID"));
            transaction_income_col.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("income"));
            transaction_pharmacistID_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("pharmacistID"));
            transaction_soldQty_col.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("soldQty"));
            transaction_pharmacistName_col.setCellValueFactory(new PropertyValueFactory<Transaction, String>("pharmacistName"));
            itemsSold_income_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("income"));
            itemsSold_itemName_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, String>("itemName"));
            itemsSold_soldQty_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Integer>("itemQty"));
            itemsSold_unitCost_col.setCellValueFactory(new PropertyValueFactory<ItemsSold, Double>("unitCost"));

            prepareTransactionTableListener();
            transactionTable.setItems(initialTransactionData());
            initializeTransactionList();
        }

        if (transactionWindowTable != null){
            transactionWindowList = new ArrayList<>();
            transactionWindow_itemName_col.setCellValueFactory(new PropertyValueFactory<>("itemName"));
            transactionWindow_sellQty_col.setCellValueFactory(new PropertyValueFactory<>("sellQty"));
            transactionWindow_unitCost_col.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
            transactionWindowTable.setItems(initialTransactionWindowData());

            Pharmacist pharma = new SQL_DataHandler().getPharmacist(pharmacistID);
            transactionWindow_PharmacistName.setText(pharma.getFirstName() + " " + pharma.getMiddleName() + " " + pharma.getLastName());
            prepareTransactionWindowComboBox();
        }

        if (transaction_searchField == null) {
            System.out.println("unitType_searchField is null");
        } else {
            transaction_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_transaction();
        }

        // Disable the search field initially
        if (transaction_searchField != null){
            transaction_searchField.setDisable(true);
        }

//        if (itemID_radioboxButton != null) {
//            itemID_radioboxButton.setOnAction(e -> enableSearchFieldIfRadioSelected());
//        } else {
//            System.out.println("itemID_radioboxButton is null");
//        }

        if (pharmacistID_radioboxButton != null) {
            pharmacistID_radioboxButton.setOnAction(e -> enableSearchFieldIfRadioSelected());
        } else {
            System.out.println("pharmacistID_radioboxButton is null");
        }

        if (transactionID_radioboxButton != null) {
            transactionID_radioboxButton.setOnAction(e -> enableSearchFieldIfRadioSelected());
        } else {
            System.out.println("transactionID_radioboxButton is null");
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
        if (restock_searchTextField == null) {
            System.out.println("field is null");
        } else {
            restock_searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Search input: " + newValue);
            });
            search_restocks();
        }

        if (inputPharmaID_nextButton != null) {
            inputPharmaID_nextButton.setDisable(true);
        }
        // Add listener to the TextField to check the pharmacist ID as it's typed
        if (input_pharmacistID_textfield != null) {
            // If not null, we proceed to add the listener
            input_pharmacistID_textfield.textProperty().addListener((observable, oldValue, newValue) -> {
                if (isPharmacistIdValid(newValue)) {
                    inputPharmaID_nextButton.setDisable(false);
                } else {
                    inputPharmaID_nextButton.setDisable(true);
                }
            });
        } else {
            System.out.println("TextField is null!");
        }

        // combo box from database
        restock_itemName_comboBoxOnAction(new ActionEvent());
        transactionItemName_comboBoxOnAction(new ActionEvent());
        itemType_cb_onAction(new ActionEvent());
        unitType_cb_onAction((new ActionEvent()));
        itemUnitType_cb_onAction((new ActionEvent()));


        ToggleGroup restockToggleGroup = new ToggleGroup();
        if (restockDateRadioButton != null || expiryDateRadioButton != null) {
            assert restockDateRadioButton != null;
            restockDateRadioButton.setToggleGroup(restockToggleGroup);
            expiryDateRadioButton.setToggleGroup(restockToggleGroup);
        }

        ToggleGroup transactionToggleGroup = new ToggleGroup();
        if (pharmacistID_radioboxButton != null || transactionID_radioboxButton != null) {
            assert pharmacistID_radioboxButton != null;
            pharmacistID_radioboxButton.setToggleGroup(transactionToggleGroup);
//            itemID_radioboxButton.setToggleGroup(transactionToggleGroup);
            transactionID_radioboxButton.setToggleGroup(transactionToggleGroup);
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

    @FXML
    void switchToTransactionWindow(ActionEvent event) {
        if (isPharmacistIdValid(input_pharmacistID_textfield.getText())) {
            // Switch to Transaction Window if ID is valid
            try {
                Stage stage = (Stage) inputPharmaID_nextButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/transactionWindow.fxml"));
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Pharmacist ID is invalid.");
            alert.showAndWait();
        }
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

    @FXML Label start_qty_label;
    @FXML Label sold_qty_label;
    @FXML Label end_qty_label;
    @FXML Label transactionCount_label;

    @FXML TableView <Restocks> recentlyRestocked;
    @FXML TableColumn <Restocks,Integer> recentlyRestocked_restockID;
    @FXML TableColumn <Restocks,Integer> recentlyRestocked_itemName;
    @FXML TableColumn <Restocks,Integer> recentlyRestocked_Qty;

    @FXML TableView <Item> fastestMovement;
    @FXML TableColumn <Item,String> fastestMovement_itemName;
    @FXML TableColumn <Item,Double> fastestMovement_movement;

    @FXML TableView <Restocks> expiry;
    @FXML
    TableColumn<Restocks, Integer> expiry_restockID;
    @FXML
    TableColumn<Restocks, Integer> expiry_expiryDate;

    @FXML TableColumn<Restocks, String> expiry_Status;

    @FXML private Label beginningBalance;
    @FXML private Label issuanceBalance;
    @FXML private Label endingBalance;
    @FXML private Label dashboardDate;

    public void loadDashboardData() {

        SQL_DataHandler handler = new SQL_DataHandler();
        double beginning = handler.getOverallBeginningBalance();  // Example value, replace with actual data
        double issuance = handler.getOverallIssuanceBalance();    // Example value, replace with actual data
        double ending = beginning - issuance;
        int start_Qty = handler.getOverallStartQuantity();
        int sold_Qty = handler.getOverallSoldQuantity();
        int end_Qty = start_Qty - sold_Qty;
        int transactionCount = handler.getTransactionCount();

        beginningBalance.setText(String.format("%.2f", beginning));
        issuanceBalance.setText(String.format("%.2f", issuance));
        endingBalance.setText(String.format("%.2f", ending));

        start_qty_label.setText("" + start_Qty);
        sold_qty_label.setText("" + sold_Qty);
        end_qty_label.setText("" + end_Qty);
        transactionCount_label.setText("" + transactionCount);
        recentlyRestocked_restockID.setCellValueFactory(new PropertyValueFactory<Restocks, Integer>("restockID"));
        recentlyRestocked_itemName.setCellValueFactory(new PropertyValueFactory<Restocks, Integer>("itemID"));
        recentlyRestocked_Qty.setCellValueFactory(new PropertyValueFactory<Restocks, Integer>("startQty"));
        recentlyRestocked.setItems(recentlyRestockedData());


        //REMOVE COMMENT IF TOP 10 FASTEST AND EXPIRY DATA WITHIN SQL HANDLER IS RESOLVED.
        fastestMovement_itemName.setCellValueFactory(new PropertyValueFactory<Item,String>("itemName"));
        fastestMovement_movement.setCellValueFactory(new PropertyValueFactory<Item,Double>("movement"));
        fastestMovement.setItems(top10FastestData());
//
        expiry_restockID.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("restockID"));
        expiry_expiryDate.setCellValueFactory(new PropertyValueFactory<Restocks,Integer>("daysBeforeExpiry"));
        expiry_Status.setCellValueFactory(new PropertyValueFactory<Restocks,String>("status"));
        expiry.setItems(top10ExpiryData());
    }

    public ObservableList<Restocks> recentlyRestockedData (){
        SQL_DataHandler handler = new SQL_DataHandler();
        Restocks types = handler.getLatestRestock();
        return FXCollections.<Restocks> observableArrayList(types);
    }

    public ObservableList<Restocks> top10ExpiryData (){
        SQL_DataHandler handler = new SQL_DataHandler();
        Restocks [] types = handler.getTop10Expiry();
        return FXCollections.<Restocks> observableArrayList(types);
    }

    public ObservableList<Item> top10FastestData (){
        SQL_DataHandler handler = new SQL_DataHandler();
        Item [] items = handler.getTop10Fastest();
        return FXCollections.<Item> observableArrayList(items);
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
