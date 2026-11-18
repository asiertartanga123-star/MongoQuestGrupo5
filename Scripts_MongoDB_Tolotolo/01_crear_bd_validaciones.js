// ============================================================================
// 01_crear_bd_validaciones.js
// Creación de la base de datos 'Tolotolo', colecciones y reglas $jsonSchema
// ============================================================================

use("Tolotolo");

// 1. Colección Salas
db.createCollection("Salas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "aforoMaximo"],
      properties: {
        nombre: {
          bsonType: "string",
          description: "Nombre descriptivo de la sala (obligatorio)"
        },
        aforoMaximo: {
          bsonType: "int",
          minimum: 1,
          description: "Capacidad máxima de la sala como entero positivo"
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});

// 2. Colección Clases
db.createCollection("Clases", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["nombre", "horas", "salaid"],
      properties: {
        nombre: {
          bsonType: "string",
          description: "Nombre de la actividad colectiva (obligatorio)"
        },
        horas: {
          // Puede estar vacío: la clase se crea sin horario y las franjas se añaden después
          // en el horario de sesiones (y se quitan todas si se borran sus sesiones).
          bsonType: "array",
          minItems: 0,
          items: {
            bsonType: "object",
            required: ["dia", "horainicio", "horafin"],
            properties: {
              dia: { bsonType: "string" },
              horainicio: { bsonType: "string" },
              horafin: { bsonType: "string" }
            }
          },
          description: "Franjas horarias semanales de la clase"
        },
        salaid: {
          bsonType: "objectId",
          description: "Referencia al _id de la colección Salas"
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});

// 3. Colección Sesiones
db.createCollection("Sesiones", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["claseid", "fecha", "plazaMax", "monitor", "asistentes"],
      properties: {
        claseid: {
          bsonType: "objectId",
          description: "Referencia al _id de la colección Clases"
        },
        fecha: {
          bsonType: "date",
          description: "Fecha y hora exacta de la sesión"
        },
        plazaMax: {
          bsonType: "int",
          minimum: 1,
          description: "Aforo máximo permitido en la sesión"
        },
        monitor: {
          bsonType: "objectId",
          description: "Referencia al _id de Trabajadores con tipo monitor"
        },
        asistentes: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["clienteid"],
            properties: {
              clienteid: { bsonType: "objectId" }
            }
          },
          description: "Listado de clientes inscritos en la sesión"
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});

// 4. Colección Clientes
db.createCollection("Clientes", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["dni", "nombre", "apellido", "fechaAlta", "mail", "contraseña", "membresia", "clases"],
      properties: {
        dni: { bsonType: "string" },
        nombre: { bsonType: "string" },
        apellido: { bsonType: "string" },
        fechaAlta: { bsonType: "date" },
        mail: { bsonType: "string" },
        "contraseña": { bsonType: "string" },
        membresia: { bsonType: "string" },
        logros: { bsonType: "array" },
        clases: {
          bsonType: "array",
          maxItems: 6,
          items: {
            bsonType: "object",
            required: ["id_clase", "nombre"],
            properties: {
              id_clase: { bsonType: "objectId" },
              nombre: { bsonType: "string" }
            }
          },
          description: "Máximo 6 clases inscritas simultáneamente por cliente"
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});

// 5. Colección Trabajadores
db.createCollection("Trabajadores", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["dni", "nombre", "apellido", "fechaAlta", "mail", "contraseña", "tipo", "clases"],
      properties: {
        dni: { bsonType: "string" },
        nombre: { bsonType: "string" },
        apellido: { bsonType: "string" },
        fechaAlta: { bsonType: "date" },
        mail: { bsonType: "string" },
        "contraseña": { bsonType: "string" },
        direccion: {
          bsonType: "array",
          items: {
            bsonType: "object",
            properties: {
              calle: { bsonType: "string" },
              cp: { bsonType: "string" },
              ciudad: { bsonType: "string" }
            }
          }
        },
        tipo: {
          enum: ["gerente", "monitor"],
          description: "Rol permitido en el sistema: gerente o monitor"
        },
        clases: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["id_clase", "nombre"],
            properties: {
              id_clase: { bsonType: "objectId" },
              nombre: { bsonType: "string" }
            }
          }
        }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});

print("Colecciones y reglas de validación creadas correctamente en 'Tolotolo'.");
