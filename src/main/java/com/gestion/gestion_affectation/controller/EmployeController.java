package com.gestion.gestion_affectation.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.gestion.gestion_affectation.model.Employe;
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

public class EmployeController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Employe> tableEmployes;

    @FXML
    private TableColumn<Employe, String> codeEmpColumn;

    @FXML
    private TableColumn<Employe, String> nomColumn;

    @FXML
    private TableColumn<Employe, String> prenomsColumn;

    @FXML
    private TableColumn<Employe, String> posteColumn;

    @FXML
    private TableColumn<Employe, Void> actionsColumn;

    private ObservableList<Employe> employeList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableEmployes.getStyleClass().add("styled-table");

        codeEmpColumn.setCellValueFactory(new PropertyValueFactory<>("codeEmp"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        prenomsColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        posteColumn.setCellValueFactory(new PropertyValueFactory<>("job"));

        configureActionsColumn();

        employeList = FXCollections.observableArrayList();
        tableEmployes.setItems(employeList);

        loadEmployesFromApi();

        // Ajout du listener pour la recherche
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                searchEmployes(newValue.trim());
            } else {
                loadEmployesFromApi(); // Revenir à la liste complète si la recherche est vide
            }
        });
    }

    private void searchEmployes(String query) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                ObservableList<Employe> searchResults = FXCollections.observableArrayList();

                HttpClient client = HttpClient.newHttpClient();

                try {
                    // Recherche par lastName
                    HttpRequest requestLastName = HttpRequest.newBuilder()
                            .uri(new URL("http://localhost:8080/employes/search/findByLastNameContainingIgnoreCase?name=" + query).toURI())
                            .GET()
                            .build();
                    HttpResponse<String> responseLastName = client.send(requestLastName, HttpResponse.BodyHandlers.ofString());
                    searchResults.addAll(parseEmployesFromJson(responseLastName.body()));
                } catch (Exception e) {
                    System.err.println("Erreur recherche par lastName : " + e.getMessage());
                }

                try {
                    // Recherche par firstName
                    HttpRequest requestFirstName = HttpRequest.newBuilder()
                            .uri(new URL("http://localhost:8080/employes/search/findByFirstNameContainingIgnoreCase?name=" + query).toURI())
                            .GET()
                            .build();
                    HttpResponse<String> responseFirstName = client.send(requestFirstName, HttpResponse.BodyHandlers.ofString());
                    searchResults.addAll(parseEmployesFromJson(responseFirstName.body()));
                } catch (Exception e) {
                    System.err.println("Erreur recherche par firstName : " + e.getMessage());
                }

                try {
                    // Recherche par codeEmp
                    HttpRequest requestCodeEmp = HttpRequest.newBuilder()
                            .uri(new URL("http://localhost:8080/employes/search/findByCodeEmpContainingIgnoreCase?codeEmp=" + query).toURI())
                            .GET()
                            .build();
                    HttpResponse<String> responseCodeEmp = client.send(requestCodeEmp, HttpResponse.BodyHandlers.ofString());
                    searchResults.addAll(parseEmployesFromJson(responseCodeEmp.body()));
                } catch (Exception e) {
                    System.err.println("Erreur recherche par codeEmp : " + e.getMessage());
                }

                // Supprimer les doublons
                ObservableList<Employe> uniqueResults = FXCollections.observableArrayList();
                searchResults.stream()
                        .distinct() // Utilise equals() et hashCode() de Employe
                        .forEach(uniqueResults::add);

                // Mettre à jour l'UI
                javafx.application.Platform.runLater(() -> employeList.setAll(uniqueResults));
                return null;
            }
        };
        new Thread(task).start();
    }

    private ObservableList<Employe> parseEmployesFromJson(String jsonResponse) {
        ObservableList<Employe> result = FXCollections.observableArrayList();
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray employesArray = jsonObject.getAsJsonObject("_embedded").getAsJsonArray("employes");

            for (JsonElement element : employesArray) {
                JsonObject empObj = element.getAsJsonObject();
                String codeEmp = empObj.getAsJsonObject("_links")
                        .getAsJsonObject("self")
                        .get("href").getAsString()
                        .substring(empObj.getAsJsonObject("_links")
                                .getAsJsonObject("self")
                                .get("href").getAsString().lastIndexOf("/") + 1);
                String firstName = empObj.get("firstName").getAsString();
                String lastName = empObj.get("lastName").getAsString();
                String job = empObj.get("job").getAsString();

                result.add(new Employe(codeEmp, firstName, lastName, job));
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing JSON : " + e.getMessage());
        }
        return result;
    }

    private void loadEmployesFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL("http://localhost:8080/employes").toURI())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray employesArray = jsonObject.getAsJsonObject("_embedded").getAsJsonArray("employes");

            employeList.clear(); // Vider la liste avant de la remplir
            for (JsonElement element : employesArray) {
                JsonObject empObj = element.getAsJsonObject();
                String codeEmp = empObj.getAsJsonObject("_links")
                        .getAsJsonObject("self")
                        .get("href").getAsString()
                        .substring(empObj.getAsJsonObject("_links")
                                .getAsJsonObject("self")
                                .get("href").getAsString().lastIndexOf("/") + 1);
                String firstName = empObj.get("firstName").getAsString();
                String lastName = empObj.get("lastName").getAsString();
                String job = empObj.get("job").getAsString();

                Employe employe = new Employe(codeEmp, firstName, lastName, job);
                employeList.add(employe);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement des employés : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Employe, Void>() {
            private final Button editButton = createButton("edit", "edit-green.png", "Modifier");
            private final Button deleteButton = createButton("delete", "delete-red.png", "Supprimer");
            private final HBox pane = new HBox(10, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);

                editButton.setOnAction(event -> {
                    Employe employe = getTableView().getItems().get(getIndex());
                    showModifyDialog(employe);
                });

                deleteButton.setOnAction(event -> {
                    Employe employe = getTableView().getItems().get(getIndex());
                    deleteEmploye(employe);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/addEmployeModal.fxml"));
            Parent root = loader.load();

            AddEmployeModalController modalController = loader.getController();
            Stage stage = new Stage();
            modalController.setStageAndEmployes(stage, employeList);

            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();
            loadEmployesFromApi();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du modal d'ajout : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showModifyDialog(Employe employe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/editEmployeModal.fxml"));
            Parent root = loader.load();

            EditEmployeModalController modalController = loader.getController();
            modalController.setEmployeData(employe, employeList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            modalController.setStage(stage);
            stage.showAndWait();
            tableEmployes.refresh();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du modal de modification : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void deleteEmploye(Employe employe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/confirmDeleteEmployeModal.fxml"));
            Parent root = loader.load();

            ConfirmDeleteEmployeModalController modalController = loader.getController();
            modalController.setConfirmationMessage("Êtes-vous sûr de vouloir supprimer l'employé : " + employe.getLastName() + " " + employe.getFirstName() + " (" + employe.getCodeEmp() + ") ?");

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
                        .uri(new URL("http://localhost:8080/employes/" + employe.getCodeEmp()).toURI())
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    employeList.remove(employe);
                    System.out.println("Employé supprimé avec succès : " + employe.getCodeEmp() + ", " + employe.getLastName() + ", " + employe.getFirstName());
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