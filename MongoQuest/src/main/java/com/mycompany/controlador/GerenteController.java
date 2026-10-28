package com.mycompany.controlador;

import java.io.IOException;
import javafx.fxml.FXML;

public class GerenteController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}