package com.gestion.gestion_affectation.controller;

import javafx.event.ActionEvent;
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
import java.util.Date;
import java.util.ResourceBundle;

public class AffectationController implements Initializable {

    @FXML
    private TableView<Affectation> tableAffectations;

    @FXML
    private TableColumn<Affectation, String> codeEmpColumn;

    @FXML
    private TableColumn<Affectation, String> codeLieuColumn;

    @FXML
    private TableColumn<Affectation, Date> dateColumn;

    @FXML
    private TableColumn<Affectation, Void> actionsColumn;

    private ObservableList<Affectation> affectationList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableAffectations.getStyleClass().add("styled-table");

        // Set up columns
        codeEmpColumn.setCellValueFactory(new PropertyValueFactory<>("codeEmp"));
        codeLieuColumn.setCellValueFactory(new PropertyValueFactory<>("codeLieu"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        configureActionsColumn();

        // Sample data for affectation
        affectationList = FXCollections.observableArrayList(
                new Affectation("E001", "L001", new Date()),
                new Affectation("E002", "L002", new Date()),
                new Affectation("E003", "L003", new Date())
        );

        tableAffectations.setItems(affectationList);
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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
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
            button.setText(id.equals("edit") ? "✏️" : "🗑️");
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
            stage.initStyle(StageStyle.UNDECORATED); // Ajouter ceci pour supprimer la barre de titre
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showModifyDialog(Affectation affectation) {
        System.out.println("Modifier l'affectation : " + affectation.getCodeEmp() + ", " + affectation.getCodeLieu() + ", " + affectation.getDate());
    }

    private void deleteAffectation(Affectation affectation) {
        System.out.println("Supprimer l'affectation : " + affectation.getCodeEmp() + ", " + affectation.getCodeLieu());
        affectationList.remove(affectation);
    }
}
