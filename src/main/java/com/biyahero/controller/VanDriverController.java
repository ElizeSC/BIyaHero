package com.biyahero.controller;

import com.biyahero.model.Van;
import com.biyahero.model.Driver;
import com.biyahero.service.VanService;
import com.biyahero.service.DriverService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class VanDriverController  {

    // Van Table
    @FXML private TableView<Van> vanTable;
    @FXML private TableColumn<Van, String> colVanId, colPlate, colModel, colStatus;
    @FXML private TableColumn<Van, Integer> colCapacity;
    @FXML private TableColumn<Van, Void> colVanAction;

    // Driver Table
    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, String> colDriverId, colDriverName, colLicense, colContact, colDriverStatus;
    @FXML private TableColumn<Driver, Void> colDriverAction;

    // UI Elements
    @FXML private TextField searchField;
    @FXML private Button btnVans, btnDrivers, btnAddEntry;
    @FXML private ComboBox<String> infoComboBox, sortComboBox;


    private final VanService vanService = new VanService();
    private final DriverService driverService = new DriverService();
    private boolean isVanView = true;

    @FXML
    public void initialize() {
        setupVanTable();
        setupDriverTable();
        setupActionButtons();
        loadData();
        setupSearch();
    }

    private void setupVanTable() {
        colVanId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("vanStatus"));
    }

    private void setupDriverTable() {
        colDriverId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colDriverName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNo"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colDriverStatus.setCellValueFactory(new PropertyValueFactory<>("driverStatus"));
    }

    private void setupActionButtons() {
        // Van Action Buttons (Edit/Delete)
        colVanAction.setCellFactory(param -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox box = new HBox(10, edit, delete);
            {
                // --- THE STYLING LINES ---
                edit.getStyleClass().addAll("btn-manage", "btn-edit-row");
                delete.getStyleClass().addAll("btn-manage", "btn-delete-row");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                // -------------------------

                edit.setOnAction(e -> openModal("/com/biyahero/view/add-van-dialog.fxml", "Edit Van", getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> {
                    vanService.deleteVan(getTableView().getItems().get(getIndex()).getVanId());
                    loadData();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Driver Action Buttons (Edit/Delete)
        colDriverAction.setCellFactory(param -> new TableCell<>() {
            private final Button edit = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox box = new HBox(10, edit, delete);
            {
                // --- THE STYLING LINES ---
                edit.getStyleClass().addAll("btn-manage", "btn-edit-row");
                delete.getStyleClass().addAll("btn-manage", "btn-delete-row");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                // -------------------------

                edit.setOnAction(e -> openModal("/com/biyahero/view/add-driver-dialog.fxml", "Edit Driver", getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> {
                    driverService.deleteDriver(getTableView().getItems().get(getIndex()).getDriverId());
                    loadData();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        vanTable.setItems(FXCollections.observableArrayList(vanService.getAllVans()));
        driverTable.setItems(FXCollections.observableArrayList(driverService.getAllDrivers()));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (isVanView) vanTable.setItems(FXCollections.observableArrayList(vanService.searchVans(newVal)));
            else driverTable.setItems(FXCollections.observableArrayList(driverService.searchDriver(newVal)));
        });
    }

    @FXML
    private void handleOpenAddModal() {
        String path = isVanView ? "/com/biyahero/view/add-van-dialog.fxml" : "/com/biyahero/view/add-driver-dialog.fxml";
        String title = isVanView ? "Add Van" : "Add Driver";
        openModal(path, title, null);
    }

    private void openModal(String path, String title, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            // Pass data based on the object type
            if (data instanceof Van) {
                ((AddVanController) loader.getController()).setExistingData((Van) data);
            } else if (data instanceof Driver) {
                ((AddDriverController) loader.getController()).setExistingData((Driver) data);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData(); // Refresh after closing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void showVanTable() { toggle(true); }
    @FXML private void showDriverTable() { toggle(false); }

    private void toggle(boolean vanView) {
        this.isVanView = vanView;

        btnVans.getStyleClass().removeAll("tab-button-active", "tab-button-inactive");
        btnDrivers.getStyleClass().removeAll("tab-button-active", "tab-button-inactive");

        if (vanView) {
            btnVans.getStyleClass().add("tab-button-active");
            btnDrivers.getStyleClass().add("tab-button-inactive");
        } else {
            btnVans.getStyleClass().add("tab-button-inactive");
            btnDrivers.getStyleClass().add("tab-button-active");
        }

        searchField.setPromptText(vanView ? "Search Van..." : "Search Driver...");
        btnAddEntry.setText(vanView ? "+ Add Van" : "+ Add Driver");
        infoComboBox.setPromptText(vanView ? "Display Van Information" : "Display Driver Information");
        sortComboBox.setPromptText(vanView ? "Van ID" : "Driver ID");

        vanTable.setVisible(vanView);
        vanTable.setManaged(vanView);
        driverTable.setVisible(!vanView);
        driverTable.setManaged(!vanView);
    }
}