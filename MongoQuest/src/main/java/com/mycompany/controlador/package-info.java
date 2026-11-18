/**
 * Capa de presentación de Tolotolo Gym: la aplicación JavaFX, un controlador por cada
 * pantalla FXML (login, registro, paneles de gerente, monitor y cliente, gestión de
 * clientes, monitores, clases y sesiones, y visor del log) y las utilidades comunes
 * ({@link com.mycompany.controlador.Registro} para el log y
 * {@link com.mycompany.controlador.GestorImagenes} para las fotos de perfil).
 * <p>
 * Los controladores no acceden a MongoDB directamente: lo hacen a través de la capa de
 * datos del paquete {@code com.mycompany.controlador.dao}.
 */
package com.mycompany.controlador;
