// ============================================================================
// 03_insertar_datos_iniciales.js
// Inserción de datos iniciales en 'Tolotolo' (incluye usuarios de prueba)
// ============================================================================

use("Tolotolo");

// 1. Salas
db.Salas.insertMany([
  {
    _id: ObjectId("650000000000000000000001"),
    nombre: "Sala Spinning",
    aforoMaximo: Int32(20)
  },
  {
    _id: ObjectId("650000000000000000000002"),
    nombre: "Sala Yoga",
    aforoMaximo: Int32(15)
  },
  {
    _id: ObjectId("650000000000000000000003"),
    nombre: "Sala Funcional",
    aforoMaximo: Int32(25)
  }
]);

// 2. Clases
db.Clases.insertMany([
  {
    _id: ObjectId("650000000000000000000011"),
    nombre: "Spinning",
    horas: [
      { dia: "lunes", horainicio: "18:00", horafin: "19:00" },
      { dia: "miércoles", horainicio: "18:00", horafin: "19:00" }
    ],
    salaid: ObjectId("650000000000000000000001")
  },
  {
    _id: ObjectId("650000000000000000000012"),
    nombre: "Yoga",
    horas: [
      { dia: "martes", horainicio: "10:00", horafin: "11:00" },
      { dia: "jueves", horainicio: "10:00", horafin: "11:00" }
    ],
    salaid: ObjectId("650000000000000000000002")
  },
  {
    _id: ObjectId("650000000000000000000013"),
    nombre: "Funcional",
    horas: [
      { dia: "viernes", horainicio: "19:00", horafin: "20:00" }
    ],
    salaid: ObjectId("650000000000000000000003")
  }
]);

// 3. Trabajadores (Gerentes y Monitores + usuario prueba empleado@empleado.com)
db.Trabajadores.insertMany([
  {
    _id: ObjectId("650000000000000000000021"),
    dni: "12345678A",
    nombre: "Miren",
    apellido: "Goikoetxea",
    fechaAlta: ISODate("2024-01-15T00:00:00.000Z"),
    mail: "miren@tolotolo.com",
    "contraseña": "$2b$10$HASH_DE_EJEMPLO_1",
    direccion: [{ calle: "Gran Vía 12", cp: "48901", ciudad: "Barakaldo" }],
    tipo: "gerente",
    clases: []
  },
  {
    _id: ObjectId("650000000000000000000022"),
    dni: "23456789B",
    nombre: "Ander",
    apellido: "Etxeberria",
    fechaAlta: ISODate("2024-03-01T00:00:00.000Z"),
    mail: "ander@tolotolo.com",
    "contraseña": "$2b$10$HASH_DE_EJEMPLO_2",
    direccion: [{ calle: "Calle Mayor 5", cp: "48902", ciudad: "Barakaldo" }],
    tipo: "monitor",
    clases: [
      { id_clase: ObjectId("650000000000000000000011"), nombre: "Spinning" },
      { id_clase: ObjectId("650000000000000000000013"), nombre: "Funcional" }
    ]
  },
  {
    _id: ObjectId("650000000000000000000023"),
    dni: "34567890C",
    nombre: "Leire",
    apellido: "Uriarte",
    fechaAlta: ISODate("2024-06-10T00:00:00.000Z"),
    mail: "leire@tolotolo.com",
    "contraseña": "$2b$10$HASH_DE_EJEMPLO_3",
    direccion: [{ calle: "Avenida Libertad 30", cp: "48903", ciudad: "Bilbao" }],
    tipo: "monitor",
    clases: [
      { id_clase: ObjectId("650000000000000000000012"), nombre: "Yoga" }
    ]
  },
  {
    _id: ObjectId("650000000000000000000099"),
    dni: "00000000A",
    nombre: "admin",
    apellido: "admin",
    fechaAlta: ISODate("2026-10-28T00:00:00.000Z"),
    mail: "admin@admin.com",
    "contraseña": "admin",
    direccion: [{ calle: "admin", cp: "00000", ciudad: "admin" }],
    tipo: "gerente",
    clases: []
  },
  {
    _id: ObjectId("650000000000000000000098"),
    dni: "22222222E",
    nombre: "Empleado",
    apellido: "Prueba",
    fechaAlta: ISODate("2026-11-18T00:00:00.000Z"),
    mail: "empleado@empleado.com",
    "contraseña": "empleado",
    direccion: [{ calle: "Calle Prueba 1", cp: "48901", ciudad: "Barakaldo" }],
    tipo: "monitor",
    clases: [
      { id_clase: ObjectId("650000000000000000000011"), nombre: "Spinning" }
    ]
  }
]);

// 4. Clientes (+ usuario prueba cliente@cliente.com)
db.Clientes.insertMany([
  {
    _id: ObjectId("650000000000000000000033"),
    dni: "67890123F",
    nombre: "Iker",
    apellido: "Martín",
    fechaAlta: ISODate("2026-01-08T00:00:00.000Z"),
    mail: "iker@correo.com",
    "contraseña": "$2b$10$HASH_DE_EJEMPLO_6",
    membresia: "normal",
    logros: [],
    clases: [
      { id_clase: ObjectId("650000000000000000000011"), nombre: "Spinning" },
      { id_clase: ObjectId("650000000000000000000012"), nombre: "Yoga" }
    ]
  },
  {
    _id: ObjectId("650000000000000000000097"),
    dni: "11111111C",
    nombre: "Cliente",
    apellido: "Prueba",
    fechaAlta: ISODate("2026-11-18T00:00:00.000Z"),
    mail: "cliente@cliente.com",
    "contraseña": "cliente",
    membresia: "normal",
    logros: [],
    clases: [
      { id_clase: ObjectId("650000000000000000000011"), nombre: "Spinning" }
    ]
  }
]);

// 5. Sesiones
db.Sesiones.insertMany([
  {
    _id: ObjectId("650000000000000000000041"),
    claseid: ObjectId("650000000000000000000011"),
    fecha: ISODate("2026-10-26T18:00:00.000Z"),
    plazaMax: Int32(20),
    monitor: ObjectId("650000000000000000000022"),
    asistentes: [
      { clienteid: ObjectId("650000000000000000000031") },
      { clienteid: ObjectId("650000000000000000000033") }
    ]
  },
  {
    _id: ObjectId("650000000000000000000042"),
    claseid: ObjectId("650000000000000000000012"),
    fecha: ISODate("2026-10-27T10:00:00.000Z"),
    plazaMax: Int32(15),
    monitor: ObjectId("650000000000000000000023"),
    asistentes: [
      { clienteid: ObjectId("650000000000000000000032") },
      { clienteid: ObjectId("650000000000000000000033") }
    ]
  },
  {
    _id: ObjectId("650000000000000000000043"),
    claseid: ObjectId("650000000000000000000013"),
    fecha: ISODate("2026-10-30T19:00:00.000Z"),
    plazaMax: Int32(25),
    monitor: ObjectId("650000000000000000000022"),
    asistentes: [
      { clienteid: ObjectId("650000000000000000000031") }
    ]
  }
]);

print("Datos iniciales insertados correctamente en 'Tolotolo'.");
