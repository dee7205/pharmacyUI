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

    // items
    @FXML private Button addItemButton;
    @FXML private Button updateItemButton;
    @FXML private Button deleteItemButton;
    @FXML private Button itemTransactHistButton;
    @FXML private ComboBox<?> item_TypeCb;
    @FXML private ComboBox<?> item_filterCb;
    @FXML private ComboBox<?> item_unitCb;


    // items table
    @FXML TableView<Item> itemTable;
    @FXML private TableColumn<Item, Double> itemUnitCost_col;
    @FXML private TableColumn<Item, String> itemName_col;
    @FXML private TableColumn<Item, Integer> itemID_col;
    @FXML private TableColumn<Item, String> itemType_col;
    @FXML private TableColumn<Item, Integer> itemUnitType_col;
    @FXML private TableColumn<Item, ?> itemMovement_col;

    int index = -1;

    // database tools
    private Connection connect;
    private PreparedStatement pr;
    private ResultSet rs;

    // connecting database
    public Connection connectDB() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/gimatagobrero", "root", " ");
            JOptionPane.showMessageDialog(null, "Connected to database");
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


    ObservableList<ItemType> initialItemTypeData(){

        SQL_DataHandler handler = new SQL_DataHandler();
        DBHandler.ItemType [] types = handler.getAllItemTypes();

        List<ItemType> list = new ArrayList<>();
        for (DBHandler.ItemType type : types){
            list.add(new ItemType(type.getItemTypeID(), type.getItemTypeName()));
        }

        return FXCollections.<ItemType> observableArrayList(list);
    }

    @FXML
    void addItemType(ActionEvent event) { //ADD ITEM TYPE
        ItemType tempItemType = new ItemType(6,itemTypeNameTextField.getText());
        itemTypeTable.getItems().add(tempItemType);
        itemTypeNameTextField.clear();
    }

    @FXML
    void deleteItemType(ActionEvent event) { //DELETE ITEM TYPE
        TableView.TableViewSelectionModel<ItemType> selectionModel = itemTypeTable.getSelectionModel();
        if (selectionModel.isEmpty()) {
            System.out.println("No Item Type Selected.");
        }
        ObservableList<Integer> list = selectionModel.getSelectedIndices();
        Integer[] selectedIndices = new Integer[list.size()];
        selectedIndices = list.toArray(selectedIndices);

        Arrays.sort(selectedIndices);
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            selectionModel.clearSelection(selectedIndices[i].intValue());
            itemTypeTable.getItems().remove(selectedIndices[i].intValue());
        }
    }

    private void itemTypeEditData(){
        itemTypeNameColumn.setCellFactory(TextFieldTableCell.<ItemType>forTableColumn());
        itemTypeNameColumn.setOnEditCommit(event ->{
            ItemType itemType = event.getTableView().getItems().get(event.getTablePosition().getRow());
            itemType.setItemTypeName(event.getNewValue());
        });
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
