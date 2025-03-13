module com.gestion.gestion_affectation {
        requires javafx.controls;
        requires javafx.fxml;
        requires com.google.gson;
    requires java.net.http;

    // Ouvre les packages nécessaires à javafx.fxml pour le chargement FXML
        opens com.gestion.gestion_affectation to javafx.fxml;
        opens com.gestion.gestion_affectation.controller to javafx.fxml;

        // Ouvre com.gestion.gestion_affectation.model à javafx.base pour PropertyValueFactory
        opens com.gestion.gestion_affectation.model to javafx.base;

        // Exporte les packages pour les rendre accessibles à d'autres modules, si nécessaire
        exports com.gestion.gestion_affectation;
        exports com.gestion.gestion_affectation.controller;
        exports com.gestion.gestion_affectation.model;
        }