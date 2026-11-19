/**
 * Aplicación de escritorio Tolotolo Gym (MongoQuest): gestión de clientes, monitores,
 * clases y sesiones de un gimnasio, con JavaFX y MongoDB.
<<<<<<< HEAD
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
=======
 *
 * <p>Paquetes del módulo:</p>
 * <ul>
 *   <li>{@code com.mycompany.controlador}: arranque de la aplicación y controladores de las
 *       diez vistas (Login, Register, Cliente, Gerente, Monitor, gestión de clientes,
 *       monitores, clases y sesiones, y visor del log), más el registro de operaciones y
 *       las fotos de los usuarios.</li>
 *   <li>{@code com.mycompany.controlador.dao}: acceso a la base de datos Tolotolo de MongoDB
 *       y excepciones de conexión, datos, duplicados y validación.</li>
 *   <li>{@code modelo}: clases de las colecciones (clientes, trabajadores, clases, sesiones,
 *       salas) y de los logros.</li>
 * </ul>
 */
module com.mycompany.mongoquest {
    // Interfaz gráfica: controles y carga de las vistas FXML.
    requires javafx.controls;
    requires javafx.fxml;

    // Driver de MongoDB (documentos BSON y cliente síncrono).
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;

    // Ficheros de log de Registro (general, errores y operaciones).
    requires java.logging;

    // FXML crea los controladores y rellena sus campos @FXML por reflexión.
    opens com.mycompany.controlador to javafx.fxml;
    // La tabla de logros del cliente lee las propiedades de modelo.Logros.
    opens modelo to javafx.base;

    // JavaFX necesita acceder a App para arrancar la aplicación.
    exports com.mycompany.controlador;
}
>>>>>>> c80656383471bb19c6f3ad214879fdd90ec2d3ae
