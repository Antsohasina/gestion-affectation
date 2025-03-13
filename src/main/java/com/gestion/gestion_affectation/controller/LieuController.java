package com.gestion.gestion_affectation.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.gestion.gestion_affectation.model.Lieu;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.stage.StageStyle;

public class LieuController implements Initializable {

    @FXML
    private TableView<Lieu> tableLieux;

    @FXML
    private TableColumn<Lieu, String> codePlaceColumn;

    @FXML
    private TableColumn<Lieu, String> designationColumn;

    @FXML
    private TableColumn<Lieu, String> provinceColumn;

    @FXML
    private TableColumn<Lieu, Void> actionsColumn;

    private ObservableList<Lieu> lieuxList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableLieux.getStyleClass().add("styled-table");

        codePlaceColumn.setCellValueFactory(new PropertyValueFactory<>("codePlace"));
        designationColumn.setCellValueFactory(new PropertyValueFactory<>("designation"));
        provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));

        configureActionsColumn();

        lieuxList = FXCollections.observableArrayList();
        tableLieux.setItems(lieuxList);

        loadLieuxFromApi(); // Charger les données au démarrage
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Lieu, Void>() {
            private final Button editButton = createButton("edit", "edit-green.png", "Modifier");
            private final Button deleteButton = createButton("delete", "delete-red.png", "Supprimer");
            private final HBox pane = new HBox(10, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    Lieu lieu = getTableView().getItems().get(getIndex());
                    showModifyDialog(lieu);
                });

                deleteButton.setOnAction(event -> {
                    Lieu lieu = getTableView().getItems().get(getIndex());
                    deleteLieu(lieu);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/addLieuModal.fxml"));
            Parent root = loader.load();

            AddLieuModalController modalController = loader.getController();
            Stage stage = new Stage();
            modalController.setStageAndLieux(stage, lieuxList);

            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED); // Ajouter ceci pour supprimer la barre de titre
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();
            loadLieuxFromApi(); // Rafraîchir après ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showModifyDialog(Lieu lieu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/editLieuModal.fxml"));
            Parent root = loader.load();

            EditLieuModalController modalController = loader.getController();
            modalController.setLieuData(lieu, lieuxList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED); // Ajouter ceci pour supprimer la barre de titre
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            modalController.setStage(stage);
            stage.showAndWait();
            tableLieux.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteLieu(Lieu lieu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/confirmDeleteLieuModal.fxml"));
            Parent root = loader.load();

            ConfirmDeleteLieuModalController modalController = loader.getController();
            modalController.setConfirmationMessage("Êtes-vous sûr de vouloir supprimer le lieu : " + lieu.getDesignation() + " (" + lieu.getCodePlace() + ") ?");

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED); // Ajouter ceci pour supprimer la barre de titre
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            modalController.setStage(stage);
            stage.showAndWait();

            if (modalController.isConfirmed()) {
                // Envoyer la requête DELETE à l'API
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URL("http://localhost:8080/places/" + lieu.getCodePlace()).toURI()) // Endpoint avec codePlace
                        .DELETE() // Requête DELETE
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) { // OK ou No Content
                    lieuxList.remove(lieu); // Supprimer de la liste locale si l'API réussit
                    System.out.println("Lieu supprimé avec succès : " + lieu.getCodePlace() + ", " + lieu.getDesignation() + ", " + lieu.getProvince());
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
    private void loadLieuxFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL("http://localhost:8080/places").toURI()) // URL réelle de l'API
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            // Parse JSON response
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray placesArray = jsonObject.getAsJsonObject("_embedded").getAsJsonArray("places");

            lieuxList.clear(); // Vider la liste actuelle
            for (JsonElement element : placesArray) {
                JsonObject placeObj = element.getAsJsonObject();
                String codePlace = placeObj.getAsJsonObject("_links")
                        .getAsJsonObject("self")
                        .get("href").getAsString()
                        .substring(placeObj.getAsJsonObject("_links")
                                .getAsJsonObject("self")
                                .get("href").getAsString().lastIndexOf("/") + 1); // Extrait le codePlace (ex. L001)
                String designation = placeObj.get("designation").getAsString();
                String province = placeObj.get("province").getAsString();

                Lieu lieu = new Lieu(codePlace, designation, province);
                lieuxList.add(lieu);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des lieux : " + e.getMessage());
            alert.showAndWait();
        }
    }
}