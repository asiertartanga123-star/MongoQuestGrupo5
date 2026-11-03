module com.mycompany.mongoquest {
    // JavaFX aporta los componentes visuales y el cargador de vistas FXML.
    requires javafx.controls;
    requires javafx.fxml;

    // FXML necesita acceso reflectivo a los controladores.
    opens com.mycompany.controlador to javafx.fxml;
    exports com.mycompany.controlador;

    // Módulos utilizados por el controlador de persistencia MongoDB.
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires java.base;
}
