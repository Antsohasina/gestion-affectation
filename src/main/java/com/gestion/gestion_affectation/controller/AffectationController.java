package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Employe;
import com.gestion.gestion_affectation.model.Place;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.gestion.gestion_affectation.model.Affectation;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AffectationController implements Initializable {

    @FXML
    private TableView<Affectation> tableAffectations;

    @FXML
    private TableColumn<Affectation, String> codeEmpColumn;

    @FXML
    private TableColumn<Affectation, String> codeLieuColumn;

    @FXML
    private TableColumn<Affectation, LocalDateTime> dateColumn;

    @FXML
    private TableColumn<Affectation, Void> actionsColumn;

    private ObservableList<Affectation> affectationList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableAffectations.getStyleClass().add("styled-table");

        // Set up columns
        codeEmpColumn.setCellValueFactory(new PropertyValueFactory<>("codeEmp"));
        codeLieuColumn.setCellValueFactory(new PropertyValueFactory<>("codePlace"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        configureActionsColumn();

        affectationList = FXCollections.observableArrayList();
        tableAffectations.setItems(affectationList);

        loadAffectationsFromApi();
    }

    private void loadAffectationsFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL("http://localhost:8080/assigned").toURI()) // Adjust to your API endpoint
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            JsonArray affectationsArray = JsonParser.parseString(jsonResponse).getAsJsonArray();

            affectationList.clear(); // Clear the list before filling
            for (JsonElement element : affectationsArray) {
                JsonObject affObj = element.getAsJsonObject();
                JsonObject idObj = affObj.getAsJsonObject("id");
                String codeEmp = idObj.get("codeEmp").getAsString();
                String codePlace = idObj.get("codePlace").getAsString();
                String dateStr = affObj.get("date").getAsString();
                LocalDateTime date = LocalDateTime.parse(dateStr);

                // Assuming Employe and Place are embedded directly in the JSON
                JsonObject employeObj = affObj.getAsJsonObject("employe");
                String empFirstName = employeObj.get("firstName").getAsString();
                String empLastName = employeObj.get("lastName").getAsString();
                String empJob = employeObj.get("job").getAsString();

                JsonObject placeObj = affObj.getAsJsonObject("place");
                String placeDesignation = placeObj.get("designation").getAsString();
                String placeProvince = placeObj.get("province").getAsString();

                Affectation affectation = new Affectation(
                        codeEmp,
                        codePlace,
                        new Employe(codeEmp, empFirstName, empLastName, empJob),
                        new Place(codePlace, placeDesignation, placeProvince),
                        date
                );
                affectationList.add(affectation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des affectations : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Affectation, Void>() {
            private final Button editButton = createButton("edit", "edit-green.png", "Modifier");
            private final Button deleteButton = createButton("delete", "delete-red.png", "Supprimer");
            private final HBox pane = new HBox(10, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    Affectation affectation = getTableView().getItems().get(getIndex());
                    showModifyDialog(affectation);
                });

                deleteButton.setOnAction(event -> {
                    Affectation affectation = getTableView().getItems().get(getIndex());
                    deleteAffectation(affectation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private Button createButton(String id, String iconName, String tooltip) {
        Button button = new Button();
        button.setId(id + "Button");
        button.getStyleClass().add("action-button");

        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/icons/" + iconName)));
            icon.setFitHeight(16);
            icon.setFitWidth(16);
            button.setGraphic(icon);
        } catch (Exception e) {
            button.setText("edit".equals(id) ? "✏️" : "🗑️");
        }

        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    @FXML
    private void handleAddButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/addAffectationModal.fxml"));
            Parent root = loader.load();

            AddAffectationModalController modalController = loader.getController();
            Stage stage = new Stage();
            modalController.setStageAndAffectations(stage, affectationList);

            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();
            loadAffectationsFromApi(); // Refresh after adding
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du modal d'ajout : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showModifyDialog(Affectation affectation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/editAffectationModal.fxml"));
            Parent root = loader.load();

            // Assuming an EditAffectationModalController exists
            EditAffectationModalController modalController = loader.getController();
            modalController.setAffectationData(affectation, affectationList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            modalController.setStage(stage);
            stage.showAndWait();
            tableAffectations.refresh(); // Refresh table after modification
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du modal de modification : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void deleteAffectation(Affectation affectation) {
        try {
            // Show confirmation modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/confirmDeleteAffectationModal.fxml"));
            Parent root = loader.load();

            // Assuming a ConfirmDeleteAffectationModalController exists
            ConfirmDeleteAffectationModalController modalController = loader.getController();
            modalController.setConfirmationMessage("Êtes-vous sûr de vouloir supprimer l'affectation : " + affectation.getCodeEmp() + " - " + affectation.getCodePlace() + " ?");

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            modalController.setStage(stage);
            stage.showAndWait();

            if (modalController.isConfirmed()) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URL("http://localhost:8080/assigned/" + affectation.getCodeEmp() + "/" + affectation.getCodePlace()).toURI())
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    affectationList.remove(affectation);
                    System.out.println("Affectation supprimée avec succès : " + affectation.getCodeEmp() + ", " + affectation.getCodePlace());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression : " + response.statusCode() + " - " + response.body());
                    alert.showAndWait();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement du modal de confirmation : " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la connexion à l'API : " + e.getMessage());
            alert.showAndWait();
        }
    }
}