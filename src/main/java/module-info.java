module com.gestion.gestion_affectation {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.gestion.gestion_affectation to javafx.fxml;
    exports com.gestion.gestion_affectation;
}