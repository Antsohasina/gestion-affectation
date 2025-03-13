package com.gestion.gestion_affectation.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

public class Affectation {
    private final StringProperty codeEmp;
    private final StringProperty codeLieu;
    private final ObjectProperty<Date> date;

    // Constructeur
    public Affectation(String codeEmp, String codeLieu, Date date) {
        this.codeEmp = new SimpleStringProperty(codeEmp);
        this.codeLieu = new SimpleStringProperty(codeLieu);
        this.date = new SimpleObjectProperty<>(date);
    }

    // Getters et Setters pour codeEmp
    public StringProperty codeEmpProperty() {
        return codeEmp;
    }

    public String getCodeEmp() {
        return codeEmp.get();
    }

    public void setCodeEmp(String codeEmp) {
        this.codeEmp.set(codeEmp);
    }

    // Getters et Setters pour codeLieu
    public StringProperty codeLieuProperty() {
        return codeLieu;
    }

    public String getCodeLieu() {
        return codeLieu.get();
    }

    public void setCodeLieu(String codeLieu) {
        this.codeLieu.set(codeLieu);
    }

    // Getters et Setters pour date
    public ObjectProperty<Date> dateProperty() {
        return date;
    }

    public Date getDate() {
        return date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }
}
