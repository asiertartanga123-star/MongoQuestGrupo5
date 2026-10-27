module com.mycompany.mongoquest {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.mongoquest to javafx.fxml;
    exports com.mycompany.mongoquest;
}
