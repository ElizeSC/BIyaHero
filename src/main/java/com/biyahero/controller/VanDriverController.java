package com.biyahero.controller;

import com.biyahero.model.Driver;
import com.biyahero.model.Van;
import com.biyahero.service.DriverService;
import com.biyahero.service.VanService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VanDriverController {

    @FXML private TableView<Van>    vanTable;
    @FXML private TableColumn<Van, String>  colVanId, colPlate, colModel, colStatus;
    @FXML private TableColumn<Van, Integer> colCapacity;
    @FXML private TableColumn<Van, Void>    colVanAction;

    @FXML private TableView<Driver>   driverTable;
    @FXML private TableColumn<Driver, String> colDriverId, colDriverName,
            colLicense, colContact, colDriverStatus;
    @FXML private TableColumn<Driver, Void>   colDriverAction;

    @FXML private TextField          searchField;
    @FXML private Button             btnVans, btnDrivers, btnAddEntry;
    @FXML private ComboBox<String>   infoComboBox, sortComboBox;

    private final VanService    vanService    = new VanService();
    private final DriverService driverService = new DriverService();
    private boolean isVanView = true;

    @FXML
    public void initialize() {
        setupVanTable();
        setupDriverTable();
        setupActionButtons();
        loadData();
        setupSearch();
        setupSortAndFilter();
    }

    // ── sort / filter ────────────────────────────────────────────────────────

    private void setupSortAndFilter() {
        infoComboBox.getItems().setAll("All", "Available", "On Trip", "Maintenance");
        infoComboBox.setValue("All");
        infoComboBox.setOnAction(e -> applyFilters());

        sortComboBox.getItems().setAll("ID (Default)", "Plate Number");
        sortComboBox.setValue("ID (Default)");
        sortComboBox.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        String status = infoComboBox.getValue();
        String sort   = sortComboBox.getValue();

        if (isVanView) {
            List<Van> vans = new ArrayList<>(
                    "All".equals(status) || status == null
                            ? vanService.getAllVans()
                            : vanService.filterByStatus(status));
            if ("Plate Number".equals(sort)) vanService.sortByPlateNumber(vans);
            else                             vanService.sortByVanId(vans);
            vanTable.setItems(FXCollections.observableArrayList(vans));
        } else {
            List<Driver> drivers = new ArrayList<>(driverService.getAllDrivers());
            if (!"All".equals(status) && status != null) {
                drivers = drivers.stream()
                        .filter(d -> status.equals(d.getDriverStatus()))
                        .collect(Collectors.toList());
            }
            if ("Plate Number".equals(sort)) driverService.sortByLicenseNo(drivers);
            else                             driverService.sortByDriverId(drivers);
            driverTable.setItems(FXCollections.observableArrayList(drivers));
        }
    }

    // ── table columns ────────────────────────────────────────────────────────

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
        colVanAction.setCellFactory(p -> new TableCell<>() {
            private final Button edit   = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox   box    = new HBox(10, edit, delete);
            {
                edit.getStyleClass().addAll("btn-manage", "btn-edit-row");
                delete.getStyleClass().addAll("btn-manage", "btn-delete-row");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                edit.setOnAction(e ->
                        openModal("/com/biyahero/view/add-van-dialog.fxml", "Edit Van",
                                getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> {
                    vanService.deleteVan(getTableView().getItems().get(getIndex()).getVanId());
                    loadData();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        colDriverAction.setCellFactory(p -> new TableCell<>() {
            private final Button edit   = new Button("Edit");
            private final Button delete = new Button("Delete");
            private final HBox   box    = new HBox(10, edit, delete);
            {
                edit.getStyleClass().addAll("btn-manage", "btn-edit-row");
                delete.getStyleClass().addAll("btn-manage", "btn-delete-row");
                box.setAlignment(javafx.geometry.Pos.CENTER);
                edit.setOnAction(e ->
                        openModal("/com/biyahero/view/add-driver-dialog.fxml", "Edit Driver",
                                getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> {
                    driverService.deleteDriver(
                            getTableView().getItems().get(getIndex()).getDriverId());
                    loadData();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        vanTable.setItems(FXCollections.observableArrayList(vanService.getAllVans()));
        driverTable.setItems(FXCollections.observableArrayList(driverService.getAllDrivers()));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                loadData();
            } else if (isVanView) {
                vanTable.setItems(FXCollections.observableArrayList(vanService.searchVans(val)));
            } else {
                driverTable.setItems(
                        FXCollections.observableArrayList(driverService.searchDriver(val)));
            }
        });
    }

    @FXML private void handleOpenAddModal() {
        openModal(isVanView
                        ? "/com/biyahero/view/add-van-dialog.fxml"
                        : "/com/biyahero/view/add-driver-dialog.fxml",
                isVanView ? "Add Van" : "Add Driver", null);
    }

    private void openModal(String path, String title, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            if (data instanceof Van)    ((AddVanController)    loader.getController()).setExistingData((Van)    data);
            if (data instanceof Driver) ((AddDriverController)  loader.getController()).setExistingData((Driver) data);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void showVanTable()    { toggle(true);  }
    @FXML private void showDriverTable() { toggle(false); }

    private void toggle(boolean vanView) {
        this.isVanView = vanView;

        btnVans.getStyleClass().removeAll("tab-button-active", "tab-button-inactive");
        btnDrivers.getStyleClass().removeAll("tab-button-active", "tab-button-inactive");
        btnVans.getStyleClass().add(vanView    ? "tab-button-active"   : "tab-button-inactive");
        btnDrivers.getStyleClass().add(vanView ? "tab-button-inactive" : "tab-button-active");

        searchField.clear();
        searchField.setPromptText(vanView ? "Search Van..." : "Search Driver...");
        btnAddEntry.setText(vanView ? "+ Add Van" : "+ Add Driver");

        // Swap filter/sort options to match the active tab
        if (vanView) {
            infoComboBox.getItems().setAll("All", "Available", "On Trip", "Maintenance");
            sortComboBox.getItems().setAll("ID (Default)", "Plate Number");
        } else {
            infoComboBox.getItems().setAll("All", "Available", "On Trip", "Off Duty");
            sortComboBox.getItems().setAll("ID (Default)", "License No.");
        }
        infoComboBox.setValue("All");
        sortComboBox.setValue("ID (Default)");

        vanTable.setVisible(vanView);    vanTable.setManaged(vanView);
        driverTable.setVisible(!vanView); driverTable.setManaged(!vanView);
        loadData();
    }
}