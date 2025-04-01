package com.gestion.gestion_affectation.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Place {
    private final StringProperty codePlace = new SimpleStringProperty();
    private final StringProperty designation = new SimpleStringProperty();
    private final StringProperty province = new SimpleStringProperty();

    // Constructeur
    public Place(String codePlace, String designation, String province) {
        this.codePlace.set(codePlace);
        this.designation.set(designation);
        this.province.set(province);
    }

    public Place() {

    }

    // Getters et Setters pour codePlace
    public StringProperty codePlaceProperty() {
        return codePlace;
    }

    public String getCodePlace() {
        return codePlace.get();
    }

    public void setCodePlace(String codePlace) {
        this.codePlace.set(codePlace);
    }

    // Getters et Setters pour designation
    public StringProperty designationProperty() {
        return designation;
    }

    public String getDesignation() {
        return designation.get();
    }

    public void setDesignation(String designation) {
        this.designation.set(designation);
    }

    // Getters et Setters pour province
    public StringProperty provinceProperty() {
        return province;
    }

    public String getProvince() {
        return province.get();
    }

    public void setProvince(String province) {
        this.province.set(province);
    }

    // Implémentation de equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return codePlace != null && codePlace.equals(place.codePlace); // Égalité basée sur codePlace
    }

    // Implémentation de hashCode()
    @Override
    public int hashCode() {
        return codePlace != null ? codePlace.hashCode() : 0;
    }
}