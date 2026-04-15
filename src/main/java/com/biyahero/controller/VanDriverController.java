package com.biyahero.controller;

import com.biyahero.model.Van;
import com.biyahero.service.VanService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class VanDriverController {

    // These MUST match the fx:id in your FXML exactly
    @FXML private TableView<Van> vanTable;
    @FXML private TableColumn<Van, String> colVanId;
    @FXML private TableColumn<Van, String> colPlate;
    @FXML private TableColumn<Van, String> colModel;
    @FXML private TableColumn<Van, Integer> colCapacity;
    @FXML private TableColumn<Van, String> colStatus;

    // Check if your FXML has fx:id="searchField" for the TextField!
    @FXML private TextField searchField;

    private final VanService vanService = new VanService();

    @FXML
    public void initialize() {
        // 1. Setup the columns
        colVanId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("vanStatus"));

        // 2. Load the data
        try {
            vanTable.setItems(FXCollections.observableArrayList(vanService.getAllVans()));
        } catch (Exception e) {
            System.err.println("Database Error: Could not fetch vans. " + e.getMessage());
        }

        // 3. ONLY setup search if searchField is not null
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newVal) -> {
                vanTable.setItems(FXCollections.observableArrayList(vanService.searchVans(newVal)));
            });
        }
    }
}