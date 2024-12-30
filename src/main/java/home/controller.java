package home;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;
import java.util.ResourceBundle;

public class controller implements Initializable {
    private Stage stage;
    private Scene scene;

    private boolean isSideBarVisible = true;

    @FXML
    private ImageView exit;
    @FXML
    private Label Menu;
    @FXML
    private Label MenuBack;
    @FXML
    private AnchorPane slider;

    @FXML
    private Button dashboardButton;
    @FXML
    private Button itemsButton;
    @FXML
    private Button pharmacistsButton;
    @FXML
    private Button restockButton;
    @FXML
    private Button statisticsButton;

    // dashboard
    @FXML
    private Label beginningBalance;
    @FXML
    private Label issuanceBalance;
    @FXML
    private Label endingBalance;
    @FXML
    private Label dashboardDate;

    // restock
    @FXML
    private Button addRestockButton;
    @FXML
    Button updateRestockButton;
    @FXML
    private Button deleteRestockButton;

    // pharmacists
    @FXML
    private Button addPharmacistsButton;
    @FXML
    private Button updatePharmacistsButton;
    @FXML
    private Button deletePharmacistsButton;
    @FXML
    private Button pharmacistTransactHistButton;

    // items
    @FXML
    private Button addItemButton;
    @FXML
    private Button updateItemButton;
    @FXML
    private Button deleteItemButton;
    @FXML
    private Button itemTransactHistButton;
    // items table
    @FXML
    private TableView<Item> itemsTable;
    @FXML
    private TableColumn<Item, Double> unitCost;
    @FXML
    private TableColumn<Item, String> itemName;
    @FXML
    private TableColumn<Item, Integer> itemNumber;
    @FXML
    private TableColumn<Item, String> itemType;
    @FXML
    private TableColumn<Item, Integer> beginningQty;

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
        } // exit image, close program

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

    public void switchToInputPharmacistIDWindow(ActionEvent event) throws IOException {
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
        double beginning = 98765.43; // Example value, replace with actual data
        double issuance = 1234.56; // Example value, replace with actual data
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
