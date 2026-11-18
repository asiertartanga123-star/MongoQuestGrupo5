/* global use, db, print */
// ============================================================================
// 05_actualizar_validacion_clases.js
// Para una base 'Tolotolo' ya creada con la versión anterior de 01_crear_bd_validaciones.js:
// deja que una clase tenga "horas" vacío (antes exigía al menos una franja y el botón
// «Crear clase» de la aplicación fallaba). No toca ningún dato.
// Se ejecuta una vez en mongosh:  mongosh --file 05_actualizar_validacion_clases.js
// ============================================================================

use("Tolotolo");

db.runCommand({
  collMod: "Clases",
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

print("Validación de Clases actualizada: una clase ya puede no tener franjas.");
