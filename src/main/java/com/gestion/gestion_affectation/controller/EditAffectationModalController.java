package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Affectation;
import com.gestion.gestion_affectation.model.Employe;
import com.gestion.gestion_affectation.model.Place;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EditAffectationModalController implements Initializable {

    @FXML
    private ComboBox<Employe> codeEmpCombo;

    @FXML
    private ComboBox<Place> codePlaceCombo;

    @FXML
    private DatePicker datePicker;

    private Stage stage;
    private Affectation affectationToEdit;
    private ObservableList<Affectation> affectationList;
    private ObservableList<Employe> employeList = FXCollections.observableArrayList();
    private ObservableList<Place> placeList = FXCollections.observableArrayList();
    private ObservableList<Employe> filteredEmployeList = FXCollections.observableArrayList();
    private ObservableList<Place> filteredPlaceList = FXCollections.observableArrayList();

    private boolean dataLoaded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les données de manière asynchrone
        loadDataAsync();

        // Rendre les ComboBox éditables
        codeEmpCombo.setEditable(true);
        codePlaceCombo.setEditable(true);

        // Initialiser les convertisseurs et écouteurs après chargement
        setupComboBoxSetup();
    }

    private void loadDataAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                loadEmployesFromApi();
                loadPlacesFromApi();
                Platform.runLater(() -> {
                    // Vérifier que les données sont chargées
                    if (!employeList.isEmpty() && !placeList.isEmpty()) {
                        filteredEmployeList.setAll(employeList);
                        filteredPlaceList.setAll(placeList);
                        codeEmpCombo.setItems(filteredEmployeList);
                        codePlaceCombo.setItems(filteredPlaceList);
                        setupComboBoxConverters(); // Configurer les convertisseurs après chargement
                        dataLoaded = true;
                        if (affectationToEdit != null) {
                            prefillFields();
                        }
                    } else {
                        showErrorAlert("Aucune donnée n'a été chargée depuis l'API.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showErrorAlert("Erreur lors du chargement des données : " + e.getMessage()));
            }
        });
    }

    private void setupComboBoxSetup() {
        // Placeholder pour initialisation basique avant chargement
        codeEmpCombo.setItems(filteredEmployeList);
        codePlaceCombo.setItems(filteredPlaceList);
    }

    private void setupComboBoxConverters() {
        codeEmpCombo.setConverter(new StringConverter<Employe>() {
            @Override
            public String toString(Employe employe) {
                return employe != null ? employe.getCodeEmp() + " - " + employe.getFirstName() + " " + employe.getLastName() : "";
            }

            @Override
            public Employe fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return employeList.stream()
                        .filter(e -> (e.getCodeEmp() + " - " + e.getFirstName() + " " + e.getLastName()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        codePlaceCombo.setConverter(new StringConverter<Place>() {
            @Override
            public String toString(Place place) {
                return place != null ? place.getCodePlace() + " - " + place.getDesignation() : "";
            }

            @Override
            public Place fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return placeList.stream()
                        .filter(p -> (p.getCodePlace() + " - " + p.getDesignation()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Écouteurs pour le filtrage
        codeEmpCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!dataLoaded) return;
            filterEmployes(newValue);
            updateSelection(codeEmpCombo, newValue, employeList);
        });

        codePlaceCombo.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!dataLoaded) return;
            filterPlaces(newValue);
            updateSelection(codePlaceCombo, newValue, placeList);
        });

        codeEmpCombo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing && codeEmpCombo.getValue() != null) {
                codeEmpCombo.getEditor().setText(codeEmpCombo.getConverter().toString(codeEmpCombo.getValue()));
            }
        });

        codePlaceCombo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing && codePlaceCombo.getValue() != null) {
                codePlaceCombo.getEditor().setText(codePlaceCombo.getConverter().toString(codePlaceCombo.getValue()));
            }
        });
    }

    private void prefillFields() {
        if (!dataLoaded || affectationToEdit == null || employeList.isEmpty() || placeList.isEmpty()) {
            Platform.runLater(() -> showErrorAlert("Les données ne sont pas prêtes pour le pré-remplissage."));
            return;
        }

        Platform.runLater(() -> {
            datePicker.setValue(affectationToEdit.getDate().toLocalDate());
            Employe currentEmploye = employeList.stream()
                    .filter(e -> e.getCodeEmp().equals(affectationToEdit.getCodeEmp()))
                    .findFirst()
                    .orElse(null);
            Place currentPlace = placeList.stream()
                    .filter(p -> p.getCodePlace().equals(affectationToEdit.getCodePlace()))
                    .findFirst()
                    .orElse(null);

            if (currentEmploye != null) {
                codeEmpCombo.getSelectionModel().select(currentEmploye);
                // Vérifier que l'éditeur est prêt avant de définir le texte
                if (codeEmpCombo.getEditor() != null && codeEmpCombo.getEditor().getText() != null) {
                    codeEmpCombo.getEditor().setText(codeEmpCombo.getConverter().toString(currentEmploye));
                }
            } else {
                showErrorAlert("Employé actuel (code : " + affectationToEdit.getCodeEmp() + ") non trouvé dans la liste.");
            }

            if (currentPlace != null) {
                codePlaceCombo.getSelectionModel().select(currentPlace);
                // Vérifier que l'éditeur est prêt avant de définir le texte
                if (codePlaceCombo.getEditor() != null && codePlaceCombo.getEditor().getText() != null) {
                    codePlaceCombo.getEditor().setText(codePlaceCombo.getConverter().toString(currentPlace));
                }
            } else {
                showErrorAlert("Lieu actuel (code : " + affectationToEdit.getCodePlace() + ") non trouvé dans la liste.");
            }
        });
    }

    private <T> void updateSelection(ComboBox<T> comboBox, String newValue, ObservableList<T> fullList) {
        if (newValue == null || newValue.isEmpty() || !dataLoaded) return;
        StringConverter<T> converter = comboBox.getConverter();
        T item = fullList.stream()
                .filter(e -> converter.toString(e).equals(newValue))
                .findFirst()
                .orElse(null);
        if (item != null) {
            comboBox.getSelectionModel().select(item);
            if (comboBox.getEditor() != null) {
                comboBox.getEditor().setText(converter.toString(item));
            }
        }
    }

    private void filterEmployes(String filterText) {
        if (!dataLoaded) return;
        filteredEmployeList.clear();
        if (filterText == null || filterText.isEmpty()) {
            filteredEmployeList.setAll(employeList);
        } else {
            String filter = filterText.toLowerCase();
            filteredEmployeList.setAll(employeList.stream()
                    .filter(e -> (e.getCodeEmp() + " - " + e.getFirstName() + " " + e.getLastName())
                            .toLowerCase().contains(filter))
                    .collect(Collectors.toList()));
        }
        codeEmpCombo.setItems(filteredEmployeList);
    }

    private void filterPlaces(String filterText) {
        if (!dataLoaded) return;
        filteredPlaceList.clear();
        if (filterText == null || filterText.isEmpty()) {
            filteredPlaceList.setAll(placeList);
        } else {
            String filter = filterText.toLowerCase();
            filteredPlaceList.setAll(placeList.stream()
                    .filter(p -> (p.getCodePlace() + " - " + p.getDesignation())
                            .toLowerCase().contains(filter))
                    .collect(Collectors.toList()));
        }
        codePlaceCombo.setItems(filteredPlaceList);
    }

    private void loadEmployesFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/employes"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
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
            } else {
                throw new RuntimeException("Échec de la requête API pour les employés : Statut " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des employés : " + e.getMessage());
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
            if (response.statusCode() == 200) {
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
            } else {
                throw new RuntimeException("Échec de la requête API pour les lieux : Statut " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement des lieux : " + e.getMessage());
        }
    }

    public void setAffectationData(Affectation affectation, ObservableList<Affectation> affectationList) {
        this.affectationToEdit = affectation;
        this.affectationList = affectationList;
        if (dataLoaded) {
            prefillFields();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleUpdate() {
        Employe selectedEmploye = codeEmpCombo.getSelectionModel().getSelectedItem();
        Place selectedPlace = codePlaceCombo.getSelectionModel().getSelectedItem();
        LocalDate localDate = datePicker.getValue();

        if (localDate == null) {
            showErrorAlert("Veuillez sélectionner une date.");
            return;
        }

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
            codeEmpCombo.getSelectionModel().select(selectedEmploye);
        }

        if (selectedPlace == null) {
            String placeText = codePlaceCombo.getEditor().getText();
            selectedPlace = placeList.stream()
                    .filter(p -> (p.getCodePlace() + " - " + p.getDesignation()).equals(placeText))
                    .findFirst()
                    .orElse(null);
            if (selectedPlace == null) {
                showErrorAlert("Veuillez sélectionner un lieu valide.");
                return;
            }
            codePlaceCombo.getSelectionModel().select(selectedPlace);
        }

        try {
            LocalDateTime dateTime = localDate.atTime(LocalTime.MIDNIGHT);
            String codeEmp = selectedEmploye.getCodeEmp();
            String codePlace = selectedPlace.getCodePlace();

            HttpClient client = HttpClient.newHttpClient();
            JsonObject json = new JsonObject();
            JsonObject idJson = new JsonObject();
            idJson.addProperty("codeEmp", codeEmp);
            idJson.addProperty("codePlace", codePlace);
            json.add("id", idJson);
            json.addProperty("date", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            JsonObject employeJson = new JsonObject();
            employeJson.addProperty("codeEmp", selectedEmploye.getCodeEmp());
            employeJson.addProperty("firstName", selectedEmploye.getFirstName());
            employeJson.addProperty("lastName", selectedEmploye.getLastName());
            employeJson.addProperty("job", selectedEmploye.getJob());
            json.add("employe", employeJson);

            JsonObject placeJson = new JsonObject();
            placeJson.addProperty("codePlace", selectedPlace.getCodePlace());
            placeJson.addProperty("designation", selectedPlace.getDesignation());
            placeJson.addProperty("province", selectedPlace.getProvince());
            json.add("place", placeJson);

            System.out.println("JSON envoyé : " + json.toString());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/assigned/" + affectationToEdit.getCodeEmp() + "/" + affectationToEdit.getCodePlace()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                affectationToEdit.getId().setCodeEmp(codeEmp);
                affectationToEdit.getId().setCodePlace(codePlace);
                affectationToEdit.setDate(dateTime);
                affectationToEdit.setEmploye(selectedEmploye);
                affectationToEdit.setPlace(selectedPlace);
                stage.close();
            } else {
                showErrorAlert("Erreur lors de la mise à jour : " + response.statusCode() + " - " + response.body());
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