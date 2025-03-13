package com.gestion.gestion_affectation.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Employe {
    private final StringProperty codeEmp = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty(); // Align with API's "firstName"
    private final StringProperty lastName = new SimpleStringProperty(); // Align with API's "lastName"
    private final StringProperty job = new SimpleStringProperty();      // Align with API's "job"

    // Default constructor (required for some frameworks or manual instantiation)
    public Employe() {}

    // Constructor with parameters
    public Employe(String codeEmp, String firstName, String lastName, String job) {
        this.codeEmp.set(codeEmp);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.job.set(job);
    }

    // Getters, Setters, and Property accessors for codeEmp
    public StringProperty codeEmpProperty() {
        return codeEmp;
    }

    public String getCodeEmp() {
        return codeEmp.get();
    }

    public void setCodeEmp(String codeEmp) {
        this.codeEmp.set(codeEmp);
    }

    // Getters, Setters, and Property accessors for firstName
    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    // Getters, Setters, and Property accessors for lastName
    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    // Getters, Setters, and Property accessors for job
    public StringProperty jobProperty() {
        return job;
    }

    public String getJob() {
        return job.get();
    }

    public void setJob(String job) {
        this.job.set(job);
    }
}