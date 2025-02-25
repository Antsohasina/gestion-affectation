package com.gestion.gestion_affectation.controller;



import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class HomeController {

    @FXML private Button btnEmploye;
    @FXML private Button btnLieu;
    @FXML private Button btnAffectation;

    @FXML private StackPane contentPane;
    @FXML private VBox contentEmploye;
    @FXML private VBox contentLieu;
    @FXML private VBox contentAffectation;

    @FXML
    public void initialize() {
        btnEmploye.setOnAction(event -> showContent(contentEmploye));
        btnLieu.setOnAction(event -> showContent(contentLieu));
        btnAffectation.setOnAction(event -> showContent(contentAffectation));
    }

    private void showContent(VBox content) {
        contentEmploye.setVisible(false);
        contentLieu.setVisible(false);
        contentAffectation.setVisible(false);

        content.setVisible(true);
    }
}
