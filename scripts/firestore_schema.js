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
      email:       { type: "string",    example: "user@gmail.com",          required: true  },
      displayName: { type: "string",    example: "Christopher Lamberti",    required: true  },
      role:        { type: "string",    example: "CLIENT | ADMIN",          required: true  },
      createdAt:   { type: "timestamp", example: "2026-05-22T00:00:00Z",    required: true  },
    },
    rules: "Propietario lee/edita el suyo. Admin lee todos. Nuevo usuario siempre crea con role=CLIENT.",
  },

  // ─── COLECCIÓN: products ───────────────────────────────────────────────────
  // Creado por: seed_firestore.js (admin manual) / futura pantalla admin
  // Leído por:  ProductRepository.kt (sync Firestore → Room local)
  products: {
    _description: "Ítems del menú. Solo el admin puede crear/editar/borrar.",
    _docId: "product_XXX  (string personalizado)",
    fields: {
      name:                  { type: "string",    example: "Hamburguesa Clásica",           required: true  },
      description:           { type: "string",    example: "Carne 100% de res...",          required: true  },
      price:                 { type: "number",    example: 4500,                            required: true  },
      imageUrl:              { type: "string",    example: "https://storage.../img.jpg",    required: false },
      ingredients:           { type: "array",     example: ["Carne", "Pan", "Queso"],       required: true  },
      estimatedTimeMinutes:  { type: "number",    example: 15,                              required: true  },
      rating:                { type: "number",    example: 4.5,                             required: true  },
      available:             { type: "boolean",   example: true,                            required: true  },
      category:              { type: "string",    example: "hamburguesas | bebidas | combos", required: true },
      createdAt:             { type: "timestamp", example: "2026-05-22T00:00:00Z",          required: true  },
      updatedAt:             { type: "timestamp", example: "2026-05-22T00:00:00Z",          required: true  },
    },
    rules: "Todos los autenticados leen. Solo ADMIN escribe.",
  },

  // ─── COLECCIÓN: orders ─────────────────────────────────────────────────────
  // Creado por: cliente al procesar un carrito
  // Leído por:  OrderRepository.kt → getOrdersByUser()
  orders: {
    _description: "Órdenes de compra. El cliente crea, el admin gestiona estados.",
    _docId: "auto-generated (string)",
    fields: {
      userId:    { type: "string",    example: "firebase-auth-uid",              required: true  },
      status:    { type: "string",    example: "PENDING|PREPARING|READY|DELIVERED|CANCELLED", required: true },
      total:     { type: "number",    example: 9000,                             required: true  },
      createdAt: { type: "timestamp", example: "2026-05-22T00:00:00Z",           required: true  },
      synced:    { type: "boolean",   example: true,                             required: false },
    },
    indexes: [
      "userId ASC + createdAt DESC  → historial del cliente",
      "status ASC + createdAt DESC  → panel del admin",
      "userId ASC + status ASC + createdAt DESC  → filtros combinados",
    ],
    rules: "Cliente crea/cancela las suyas (solo si status=PENDING). Admin gestiona todo.",
  },

  // ─── COLECCIÓN: order_items ────────────────────────────────────────────────
  // Creado por: cliente al confirmar orden (junto con orders)
  // Leído por:  OrderDao.kt → getItemsByOrder()
  order_items: {
    _description: "Ítems de cada orden. Referencia a products y orders.",
    _docId: "auto-generated (string)",
    fields: {
      orderId:   { type: "string",  example: "order-doc-id",    required: true },
      productId: { type: "string",  example: "product_001",     required: true },
      quantity:  { type: "number",  example: 2,                 required: true },
      unitPrice: { type: "number",  example: 4500,              required: true },
    },
    rules: "Sigue permisos de su orden padre.",
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
    console.log(`   DocID: ${config._docId}`);
    console.log("   Campos:");

    for (const [field, meta] of Object.entries(config.fields)) {
      const req = meta.required ? "✔" : "○";
      console.log(`     ${req}  ${field.padEnd(22)} ${meta.type.padEnd(10)}  ej: ${meta.example}`);
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
