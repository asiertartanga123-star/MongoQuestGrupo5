/**
 * Módulo de MongoQuest: aplicación JavaFX de gestión de un gimnasio (clientes, monitores y clases)
 * que guarda sus datos en MongoDB.
 */
module com.mycompany.mongoquest {
    // JavaFX aporta los componentes visuales y el cargador de vistas FXML.
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Módulos utilizados por el controlador de persistencia MongoDB.
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;

    // Registro de eventos (logs) de la aplicación.
    requires java.logging;

    // FXML necesita acceso reflectivo a los controladores.
    opens com.mycompany.controlador to javafx.fxml;

    // Las tablas (TableView / PropertyValueFactory) leen los getters del modelo por reflexión.
    opens modelo to javafx.base;

    // Paquetes exportados: son los que Javadoc documenta.
    exports com.mycompany.controlador;
    exports com.mycompany.controlador.dao;
    exports modelo;
}
