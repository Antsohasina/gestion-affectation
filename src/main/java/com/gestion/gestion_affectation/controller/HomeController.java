

package com.gestion.gestion_affectation.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import java.io.IOException;

    public class HomeController {
        @FXML private Button btnEmploye;
        @FXML private Button btnLieu;
        @FXML private Button btnAffectation;
        @FXML
        private StackPane contentPane; // Le StackPane dans le FXML

        // Méthode pour charger et afficher le contenu des employés
        @FXML
        private void showEmployeContent() {
            loadContent("employe.fxml");
        }

        // Méthode pour charger et afficher le contenu des lieux
        @FXML
        private void showLieuContent() {
            loadContent("lieu.fxml");
        }

        // Méthode pour charger et afficher le contenu des affectations
        @FXML
        private void showAffectationContent() {
            loadContent("affectation.fxml");
        }

        // Méthode générique pour charger un fichier FXML dans le StackPane
        private void loadContent(String fxmlFile) {
            try {
                // Charge le fichier FXML
               // FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestion/gestion_affectation/fxml/" + fxmlFile));
                String fxmlPath = "/com/gestion/gestion_affectation/fxml/" + fxmlFile;
                System.out.println("Chargement de : " + fxmlPath);
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                System.out.println("Chargement réussi pour : " + fxmlFile);
                // Vide le StackPane et ajoute le nouveau contenu
                contentPane.getChildren().clear();
                contentPane.getChildren().add(root);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de " + fxmlFile);
                e.printStackTrace();
            }
        }

        // Méthode qui est appelée au lancement de l'application pour définir le contenu par défaut
        @FXML
        public void initialize() {
            // Par défaut, afficher la vue des employés
            showEmployeContent();
        }
    }
