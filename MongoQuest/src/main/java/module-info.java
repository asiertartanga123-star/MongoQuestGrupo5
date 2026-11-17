module com.mycompany.mongoquest {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.controlador to javafx.fxml;
    exports com.mycompany.controlador;
    opens modelo to javafx.base;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires java.base;
}
