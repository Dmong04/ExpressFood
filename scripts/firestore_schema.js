/**
 * firestore_schema.js
 * Schema completo de la base de datos Firestore para ExpressFood.
 * Refleja exactamente las colecciones, documentos y campos del proyecto.
 *
 * USO (para ver el schema en consola):
 *   node scripts/firestore_schema.js
 */

const schema = {

  // ─── COLECCIÓN: users ──────────────────────────────────────────────────────
  // Creado por: UserFirestoreService.kt → createUser()
  // Leído por:  UserFirestoreService.kt → getUser()
  users: {
    _description: "Perfiles de usuario. Cada documento = un usuario autenticado.",
    _docId: "Firebase Auth UID  (string)",
    fields: {
      firstName:    { type: "string",    example: "Christopher",               required: true  },
      lastName:     { type: "string",    example: "Lamberti",                  required: true  },
      phone:        { type: "string",    example: "+506 8888 8888",            required: false },
      profilePhoto: { type: "string",    example: "https://storage.../photo",  required: false },
      role:         { type: "string",    example: "CLIENT | ADMIN",            required: true  },
      address:      { type: "string",    example: "San José, Costa Rica",      required: false },
      createdAt:    { type: "timestamp", example: "2026-05-22T00:00:00Z",      required: true  },
    },
    rules: "Propietario lee/edita el suyo. Admin lee todos. Nuevo usuario siempre crea con role=CLIENT.",
  },

  // ─── COLECCIÓN: items ──────────────────────────────────────────────────────
  // Creado por: seed_firestore.js (admin manual) / futura pantalla admin
  // Leído por:  ItemFirestoreService.kt → getActiveItems()
  items: {
    _description: "Ítems del menú. Solo el admin puede crear/editar/borrar.",
    _docId: "item_XXX  (string personalizado)",
    fields: {
      title:       { type: "string",    example: "Hamburguesa Clásica",        required: true  },
      description: { type: "string",    example: "Carne 100% de res...",       required: true  },
      price:       { type: "number",    example: 4500,                         required: true  },
      prepTime:    { type: "number",    example: 15,                           required: true  },
      imgUrl:      { type: "string",    example: "https://storage.../img.jpg", required: false },
      active:      { type: "boolean",   example: true,                         required: true  },
      synced:      { type: "boolean",   example: true,                         required: false },
    },
    rules: "Todos los autenticados leen. Solo ADMIN escribe.",
  },

  // ─── COLECCIÓN: orders ─────────────────────────────────────────────────────
  // Creado por: cliente al procesar un carrito
  // Leído por:  OrderRepository.kt → getOrdersByClient()
  orders: {
    _description: "Órdenes de compra. El cliente crea, el admin gestiona estados.",
    _docId: "auto-generated (string)",
    fields: {
      clientId:   { type: "string",    example: "firebase-auth-uid",                          required: true  },
      date:       { type: "timestamp", example: "2026-05-22T00:00:00Z",                       required: true  },
      time:       { type: "string",    example: "14:30",                                      required: true  },
      status:     { type: "string",    example: "PENDING|PREPARING|READY|DELIVERED|CANCELLED", required: true  },
      totalPrice: { type: "number",    example: 9000,                                         required: true  },
      synced:     { type: "boolean",   example: true,                                         required: false },
    },
    indexes: [
      "clientId ASC + date DESC  → historial del cliente",
      "status ASC + date DESC    → panel del admin",
      "clientId ASC + status ASC + date DESC  → filtros combinados",
    ],
    rules: "Cliente crea/cancela las suyas (solo si status=PENDING). Admin gestiona todo.",
  },

  // ─── COLECCIÓN: orderDetail ────────────────────────────────────────────────
  // Creado por: cliente al confirmar orden (junto con orders)
  // Leído por:  OrderDao.kt → getDetailsByOrder()
  orderDetail: {
    _description: "Detalle de cada orden. Referencia a items y orders.",
    _docId: "auto-generated (string)",
    fields: {
      orderId:   { type: "string",  example: "order-doc-id",  required: true  },
      itemId:    { type: "string",  example: "item_001",       required: true  },
      quantity:  { type: "number",  example: 2,               required: true  },
      itemPrice: { type: "number",  example: 4500,            required: true  },
      rating:    { type: "number",  example: 4.5,             required: false },
    },
    rules: "Sigue permisos de su orden padre. Cliente puede actualizar rating si status=DELIVERED.",
  },

  // ─── COLECCIÓN: cartItem ───────────────────────────────────────────────────
  // Manejado por: cliente en tiempo real antes de confirmar la orden
  cartItem: {
    _description: "Carrito de compras del cliente. Se limpia al confirmar la orden.",
    _docId: "auto-generated (string)",
    fields: {
      clientId: { type: "string", example: "firebase-auth-uid", required: true },
      itemId:   { type: "string", example: "item_001",          required: true },
      quantity: { type: "number", example: 2,                   required: true },
    },
    indexes: [
      "clientId ASC  → carrito del cliente",
    ],
    rules: "Cada cliente gestiona solo sus propios ítems.",
  },

  // ─── COLECCIÓN: storage ────────────────────────────────────────────────────
  // Metadatos de imágenes subidas a Firebase Storage
  storage: {
    _description: "Mapeo de rutas de Firebase Storage a ítems del menú.",
    _docId: "path de Storage (string)",
    fields: {
      itemId: { type: "string", example: "item_001", required: true },
    },
    rules: "Cualquier autenticado puede leer. Solo ADMIN escribe.",
  },

};

// ─── Print en consola ─────────────────────────────────────────────────────────

function printSchema() {
  console.log("\n╔══════════════════════════════════════════════════════╗");
  console.log("║         EXPRESSFOOD — FIRESTORE SCHEMA               ║");
  console.log("╚══════════════════════════════════════════════════════╝\n");

  for (const [collection, config] of Object.entries(schema)) {
    console.log(`📁 ${collection.toUpperCase()}`);
    console.log(`   ${config._description}`);
    if (config._docId) console.log(`   DocID: ${config._docId}`);
    console.log("   Campos:");

    for (const [field, meta] of Object.entries(config.fields)) {
      const req = meta.required ? "✔" : "○";
      console.log(`     ${req}  ${field.padEnd(14)} ${meta.type.padEnd(10)}  ej: ${meta.example}`);
    }

    if (config.indexes) {
      console.log("   Índices:");
      config.indexes.forEach(i => console.log(`     → ${i}`));
    }

    console.log(`   Reglas: ${config.rules}`);
    console.log();
  }

  console.log("✔ = requerido   ○ = opcional\n");
}

printSchema();
