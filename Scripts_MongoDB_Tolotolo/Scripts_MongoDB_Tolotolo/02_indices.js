// ============================================================================
// 02_indices.js
// Creación de índices únicos y de rendimiento en la base de datos 'Tolotolo'
// ============================================================================

use("Tolotolo");

// Índices en Salas
db.Salas.createIndex({ nombre: 1 }, { unique: true, name: "idx_salas_nombre_unique" });

// Índices en Clases
db.Clases.createIndex({ nombre: 1 }, { unique: true, name: "idx_clases_nombre_unique" });
db.Clases.createIndex({ salaid: 1 }, { name: "idx_clases_salaid" });

// Índices en Sesiones (consultas por clase, fecha, monitor y asistentes)
db.Sesiones.createIndex({ claseid: 1, fecha: 1 }, { name: "idx_sesiones_clase_fecha" });
db.Sesiones.createIndex({ monitor: 1, fecha: 1 }, { name: "idx_sesiones_monitor_fecha" });
db.Sesiones.createIndex({ "asistentes.clienteid": 1 }, { name: "idx_sesiones_asistentes" });

// Índices en Clientes (login por mail y unicidad de DNI/correo)
db.Clientes.createIndex({ mail: 1 }, { unique: true, name: "idx_clientes_mail_unique" });
db.Clientes.createIndex({ dni: 1 }, { unique: true, name: "idx_clientes_dni_unique" });
db.Clientes.createIndex({ "clases.id_clase": 1 }, { name: "idx_clientes_clases" });

// Índices en Trabajadores (login por mail y filtrado por rol)
db.Trabajadores.createIndex({ mail: 1 }, { unique: true, name: "idx_trabajadores_mail_unique" });
db.Trabajadores.createIndex({ dni: 1 }, { unique: true, name: "idx_trabajadores_dni_unique" });
db.Trabajadores.createIndex({ tipo: 1 }, { name: "idx_trabajadores_tipo" });

print("Índices creados correctamente en 'Tolotolo'.");
