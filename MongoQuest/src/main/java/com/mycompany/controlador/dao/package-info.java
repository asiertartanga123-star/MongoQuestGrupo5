/**
 * Capa de acceso a datos (patrón DAO) sobre MongoDB.
 * <p>
 * {@link com.mycompany.controlador.dao.dao} define las operaciones y
 * {@link com.mycompany.controlador.dao.daoImplementacio} las implementa con el driver
 * síncrono de MongoDB y una única conexión compartida. Cada operación queda en el log de
 * operaciones, y los errores del driver se traducen a
 * {@link com.mycompany.controlador.dao.DatosException} y sus subclases
 * ({@link com.mycompany.controlador.dao.DuplicadoException},
 * {@link com.mycompany.controlador.dao.ValidacionException} y
 * {@link com.mycompany.controlador.dao.ConexionException}), que la capa de presentación
 * captura para avisar al usuario.
 */
package com.mycompany.controlador.dao;
