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
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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
    // items table
    @FXML private TableView<Item> itemsTable;
    @FXML private TableColumn<Item, Double> unitCost;
    @FXML private TableColumn<Item, String> itemName;
    @FXML private TableColumn<Item, Integer> itemNumber;
    @FXML private TableColumn<Item, String> itemType;
    @FXML private TableColumn<Item, Integer> beginningQty;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//       // table view (items)
//        itemNumber.setCellValueFactory(new PropertyValueFactory<Item, Integer>("itemNumber"));
//        itemName.setCellValueFactory(new PropertyValueFactory<Item, String>("itemName"));
//        itemType.setCellValueFactory(new PropertyValueFactory<Item, String>("itemType"));
//        beginningQty.setCellValueFactory(new PropertyValueFactory<Item, Integer>("beginningQty"));
//        unitCost.setCellValueFactory(new PropertyValueFactory<Item, Double>("unitCost"));
//
//        itemsTable.setItems(itemsList);
//        // end of table view (items)

        exit.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Confirmation");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Click OK to exit or Cancel to stay.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.exit(0);
            }
        }); // exit image, close program

        slider.setTranslateX(-176);

        Menu.setOnMouseClicked(event -> toggleSideBar());
        MenuBack.setOnMouseClicked(event -> toggleSideBar());
    }

    public void switchScene(String fxmlFileName, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFileName));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
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

    public void toggleSideBar() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.4));
        slide.setNode(slider);

        if (isSideBarVisible) {
            slide.setToX(-176);
            slide.setOnFinished((ActionEvent e) -> {
                Menu.setVisible(true);
                MenuBack.setVisible(false);
            });
        } else {
            slide.setToX(0);
            slide.setOnFinished((ActionEvent e) -> {
                Menu.setVisible(false);
                MenuBack.setVisible(true);
            });
        }

        slide.play();
        isSideBarVisible = !isSideBarVisible;
    }

}
