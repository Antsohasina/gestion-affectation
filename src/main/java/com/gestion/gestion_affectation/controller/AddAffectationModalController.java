package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Affectation;
import com.gestion.gestion_affectation.model.Employe;
import com.gestion.gestion_affectation.model.Place;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddAffectationModalController implements Initializable {

    @FXML
    private ComboBox<Employe> codeEmpCombo;

    @FXML
    private ComboBox<Place> codeLieuCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private Stage stage;
    private ObservableList<Affectation> affectationList;
    private ObservableList<Employe> employeList = FXCollections.observableArrayList();
    private ObservableList<Place> placeList = FXCollections.observableArrayList();
    private ObservableList<Employe> filteredEmployeList = FXCollections.observableArrayList();
    private ObservableList<Place> filteredPlaceList = FXCollections.observableArrayList();

    // Flag to prevent infinite loop in updateSelection
    private boolean updatingSelection = false;

    public void setStageAndAffectations(Stage stage, ObservableList<Affectation> affectationList) {
        this.stage = stage;
        this.affectationList = affectationList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les employés et lieux existants depuis l'API
        loadEmployesFromApi();
        loadPlacesFromApi();

        // Rendre les ComboBox éditables
        codeEmpCombo.setEditable(true);
        codeLieuCombo.setEditable(true);

        // Initialiser les listes filtrées avec toutes les données
        filteredEmployeList.addAll(employeList);
        filteredPlaceList.addAll(placeList);

        // Remplir les ComboBox avec les listes filtrées
        codeEmpCombo.setItems(filteredEmployeList);
        codeLieuCombo.setItems(filteredPlaceList);

        // Convertisseur pour afficher les éléments dans les ComboBox
        codeEmpCombo.setConverter(new StringConverter<Employe>() {
            @Override
            public String toString(Employe employe) {
                return employe != null ? employe.getCodeEmp() + " - " + employe.getFirstName() + " " + employe.getLastName() : "";
            }

            @Override
            public Employe fromString(String string) {
                return employeList.stream()
                        .filter(e -> (e.getCodeEmp() + " - " + e.getFirstName() + " " + e.getLastName()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        codeLieuCombo.setConverter(new StringConverter<Place>() {
            @Override
            public String toString(Place place) {
                return place != null ? place.getCodePlace() + " - " + place.getDesignation() : "";
            }

            @Override
            public Place fromString(String string) {
                return placeList.stream()
                        .filter(p -> (p.getCodePlace() + " - " + p.getDesignation()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Listener pour filtrer les employés en fonction de la saisie
        codeEmpCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingSelection) { // Avoid processing if update is triggered by selection
                filterEmployes(newValue);
                if (!codeEmpCombo.isShowing() && newValue != null && !newValue.isEmpty()) {
                    codeEmpCombo.show(); // Afficher la liste si elle n'est pas déjà visible
                }
                updateSelection(codeEmpCombo, newValue, employeList);
            }
        });

        // Listener pour filtrer les lieux en fonction de la saisie
        codeLieuCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingSelection) { // Avoid processing if update is triggered by selection
                filterPlaces(newValue);
                if (!codeLieuCombo.isShowing() && newValue != null && !newValue.isEmpty()) {
                    codeLieuCombo.show(); // Afficher la liste si elle n'est pas déjà visible
                }
                updateSelection(codeLieuCombo, newValue, placeList);
            }
        });

        // Gérer la fermeture de la liste déroulante pour synchroniser le texte
        codeEmpCombo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing) {
                Employe selected = codeEmpCombo.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    codeEmpCombo.getEditor().setText(toString(selected));
                }
            }
        });

        codeLieuCombo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing) {
                Place selected = codeLieuCombo.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    codeLieuCombo.getEditor().setText(toString(selected));
                }
            }
        });
    }

    private String toString(Employe employe) {
        return employe != null ? employe.getCodeEmp() + " - " + employe.getFirstName() + " " + employe.getLastName() : "";
    }

    private String toString(Place place) {
        return place != null ? place.getCodePlace() + " - " + place.getDesignation() : "";
    }

    private <T> void updateSelection(ComboBox<T> comboBox, String newValue, ObservableList<T> fullList) {
        if (newValue != null && !newValue.isEmpty()) {
            updatingSelection = true; // Set flag to prevent re-entry
            try {
                StringConverter<T> converter = comboBox.getConverter();
                T item = fullList.stream()
                        .filter(e -> converter.toString(e).equals(newValue))
                        .findFirst()
                        .orElse(null);
                if (item != null) {
                    comboBox.getSelectionModel().select(item);
                }
            } finally {
                updatingSelection = false; // Reset flag in finally block to ensure it always resets
            }
        }
    }

    private void filterEmployes(String filterText) {
        filteredEmployeList.clear();
        if (filterText == null || filterText.isEmpty()) {
            filteredEmployeList.addAll(employeList);
        } else {
            String filter = filterText.toLowerCase();
            filteredEmployeList.addAll(employeList.stream()
                    .filter(e -> (e.getCodeEmp() + " - " + e.getFirstName() + " " + e.getLastName())
                            .toLowerCase().contains(filter))
                    .collect(Collectors.toList()));
        }
    }

    private void filterPlaces(String filterText) {
        filteredPlaceList.clear();
        if (filterText == null || filterText.isEmpty()) {
            filteredPlaceList.addAll(placeList);
        } else {
            String filter = filterText.toLowerCase();
            filteredPlaceList.addAll(placeList.stream()
                    .filter(p -> (p.getCodePlace() + " - " + p.getDesignation())
                            .toLowerCase().contains(filter))
                    .collect(Collectors.toList()));
        }
    }

    private void loadEmployesFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/employes"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray employesArray = jsonResponse.getAsJsonObject("_embedded").getAsJsonArray("employes");

            employeList.clear();
            for (JsonElement element : employesArray) {
                JsonObject empObj = element.getAsJsonObject();
                String codeEmp = empObj.getAsJsonObject("_links").getAsJsonObject("self").get("href").getAsString()
                        .substring(empObj.getAsJsonObject("_links").getAsJsonObject("self").get("href").getAsString().lastIndexOf("/") + 1);
                String firstName = empObj.get("firstName").getAsString();
                String lastName = empObj.get("lastName").getAsString();
                String job = empObj.get("job").getAsString();

                Employe employe = new Employe(codeEmp, firstName, lastName, job);
                employeList.add(employe);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du chargement des employés : " + e.getMessage());
        }
    }

    private void loadPlacesFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/places"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray placesArray = jsonResponse.getAsJsonObject("_embedded").getAsJsonArray("places");

            placeList.clear();
            for (JsonElement element : placesArray) {
                JsonObject placeObj = element.getAsJsonObject();
                String codePlace = placeObj.getAsJsonObject("_links").getAsJsonObject("self").get("href").getAsString()
                        .substring(placeObj.getAsJsonObject("_links").getAsJsonObject("self").get("href").getAsString().lastIndexOf("/") + 1);
                String designation = placeObj.get("designation").getAsString();
                String province = placeObj.get("province").getAsString();

                Place place = new Place(codePlace, designation, province);
                placeList.add(place);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors du chargement des lieux : " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        Employe selectedEmploye = codeEmpCombo.getSelectionModel().getSelectedItem();
        Place selectedPlace = codeLieuCombo.getSelectionModel().getSelectedItem();
        LocalDate localDate = datePicker.getValue();

        // Vérifier si une date est sélectionnée
        if (localDate == null) {
            showErrorAlert("Veuillez sélectionner une date.");
            return;
        }

        // Si aucune sélection explicite, tenter de matcher avec le texte saisi
        if (selectedEmploye == null) {
            String empText = codeEmpCombo.getEditor().getText();
            selectedEmploye = employeList.stream()
                    .filter(e -> (e.getCodeEmp() + " - " + e.getFirstName() + " " + e.getLastName()).equals(empText))
                    .findFirst()
                    .orElse(null);
            if (selectedEmploye == null) {
                showErrorAlert("Veuillez sélectionner un employé valide.");
                return;
            }
            codeEmpCombo.getSelectionModel().select(selectedEmploye); // Mettre à jour la sélection
        }

        if (selectedPlace == null) {
            String placeText = codeLieuCombo.getEditor().getText();
            selectedPlace = placeList.stream()
                    .filter(p -> (p.getCodePlace() + " - " + p.getDesignation()).equals(placeText))
                    .findFirst()
                    .orElse(null);
            if (selectedPlace == null) {
                showErrorAlert("Veuillez sélectionner un lieu valide.");
                return;
            }
            codeLieuCombo.getSelectionModel().select(selectedPlace); // Mettre à jour la sélection
        }

        try {
            LocalDateTime dateTime = localDate.atTime(LocalTime.MIDNIGHT);
            String codeEmp = selectedEmploye.getCodeEmp();
            String codeLieu = selectedPlace.getCodePlace();

            HttpClient client = HttpClient.newHttpClient();
            JsonObject json = new JsonObject();
            JsonObject idJson = new JsonObject();
            idJson.addProperty("codeEmp", codeEmp);
            idJson.addProperty("codePlace", codeLieu);
            json.add("id", idJson);
            json.addProperty("date", dateTime.toString());

            JsonObject employeJson = new JsonObject();
            employeJson.addProperty("codeEmp", codeEmp);
            json.add("employe", employeJson);

            JsonObject placeJson = new JsonObject();
            placeJson.addProperty("codePlace", codeLieu);
            json.add("place", placeJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/assigned"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Affectation newAffectation = new Affectation(codeEmp, codeLieu, selectedEmploye, selectedPlace, dateTime);
                affectationList.add(newAffectation);
                stage.close();
            } else {
                showErrorAlert("Erreur lors de l'ajout : " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la connexion à l'API : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}