/**
 * Aplicación de escritorio Tolotolo Gym (MongoQuest): gestión de clientes, monitores,
 * clases y sesiones de un gimnasio, con JavaFX y MongoDB.
 */
module com.mycompany.mongoquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires java.logging;

    opens com.mycompany.controlador to javafx.fxml;
    opens modelo to javafx.base;
    exports com.mycompany.controlador;
}