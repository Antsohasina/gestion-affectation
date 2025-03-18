package com.gestion.gestion_affectation.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;

public class Affectation {
    // Classes pour correspondre à la structure JSON
    public static class AffectationId {
        private final StringProperty codeEmp;
        private final StringProperty codePlace;

        public AffectationId(String codeEmp, String codePlace) {
            this.codeEmp = new SimpleStringProperty(codeEmp);
            this.codePlace = new SimpleStringProperty(codePlace);
        }

        public StringProperty codeEmpProperty() {
            return codeEmp;
        }

        public String getCodeEmp() {
            return codeEmp.get();
        }

        public void setCodeEmp(String codeEmp) {
            this.codeEmp.set(codeEmp);
        }

        public StringProperty codePlaceProperty() {
            return codePlace;
        }

        public String getCodePlace() {
            return codePlace.get();
        }

        public void setCodePlace(String codePlace) {
            this.codePlace.set(codePlace);
        }
    }

    // Attributs de la classe Affectation
    private final ObjectProperty<AffectationId> id;
    private final ObjectProperty<Employe> employe;
    private final ObjectProperty<Place> place;
    private final ObjectProperty<LocalDateTime> date;

    // Constructeur pour créer depuis les données de l'API
    public Affectation(String codeEmp, String codePlace, Employe employe, Place place, LocalDateTime date) {
        this.id = new SimpleObjectProperty<>(new AffectationId(codeEmp, codePlace));
        this.employe = new SimpleObjectProperty<>(employe);
        this.place = new SimpleObjectProperty<>(place);
        this.date = new SimpleObjectProperty<>(date);
    }

    // Constructeur pratique qui prend l'objet JSON désérialisé
    public Affectation(AffectationId id, Employe employe, Place place, LocalDateTime date) {
        this.id = new SimpleObjectProperty<>(id);
        this.employe = new SimpleObjectProperty<>(employe);
        this.place = new SimpleObjectProperty<>(place);
        this.date = new SimpleObjectProperty<>(date);
    }

    // Getters et Setters
    public ObjectProperty<AffectationId> idProperty() {
        return id;
    }

    public AffectationId getId() {
        return id.get();
    }

    public void setId(AffectationId id) {
        this.id.set(id);
    }

    public ObjectProperty<Employe> employeProperty() {
        return employe;
    }

    public Employe getEmploye() {
        return employe.get();
    }

    public void setEmploye(Employe employe) {
        this.employe.set(employe);
    }

    public ObjectProperty<Place> placeProperty() {
        return place;
    }

    public Place getPlace() {
        return place.get();
    }

    public void setPlace(Place place) {
        this.place.set(place);
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(LocalDateTime date) {
        this.date.set(date);
    }

    // Méthodes utilitaires pour l'affichage

    // Retourne directement le code de l'employé
    public String getCodeEmp() {
        return getId().getCodeEmp();
    }

    // Retourne directement le code du lieu
    public String getCodePlace() {
        return getId().getCodePlace();
    }

    // Propriétés pour faciliter l'affichage dans les TableView
    public StringProperty codeEmpProperty() {
        return getId().codeEmpProperty();
    }

    public StringProperty codePlaceProperty() {
        return getId().codePlaceProperty();
    }
}