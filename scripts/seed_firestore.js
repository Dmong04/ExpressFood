/**
 * seed_firestore.js
 * Pobla Firestore con datos iniciales de ítems del menú y un usuario admin.
 *
 * USO:
 *   1. Instalar dependencias:  npm install firebase-admin
 *   2. Descargar la Service Account Key desde Firebase Console
 *      → Project Settings → Service Accounts → Generate new private key
 *   3. Guardarla como scripts/service-account-key.json  (NO subir a git)
 *   4. Correr: node scripts/seed_firestore.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('./service-account-key.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

// ─── Datos iniciales ─────────────────────────────────────────────────────────

const items = [
  {
    id: 'item_001',
    title: 'Hamburguesa Clásica',
    description: 'Carne 100% de res, lechuga fresca, tomate, queso cheddar y salsa especial.',
    price: 4500,
    prepTime: 15,
    imgUrl: '',
    active: true,
  },
  {
    id: 'item_002',
    title: 'Hamburguesa BBQ',
    description: 'Doble carne, tocino crocante, cebolla caramelizada y salsa BBQ ahumada.',
    price: 5800,
    prepTime: 18,
    imgUrl: '',
    active: true,
  },
  {
    id: 'item_003',
    title: 'Papas Fritas',
    description: 'Papas crujientes con sal y condimentos. Porción grande.',
    price: 2000,
    prepTime: 8,
    imgUrl: '',
    active: true,
  },
  {
    id: 'item_004',
    title: 'Refresco',
    description: 'Refresco en lata 355ml. Elige entre: Cola, Naranja, Limón.',
    price: 1200,
    prepTime: 1,
    imgUrl: '',
    active: true,
  },
  {
    id: 'item_005',
    title: 'Combo Clásico',
    description: 'Hamburguesa Clásica + Papas Fritas + Refresco.',
    price: 6500,
    prepTime: 18,
    imgUrl: '',
    active: true,
  },
];

// ─── Funciones de seed ────────────────────────────────────────────────────────

async function seedItems() {
  console.log('📦 Insertando ítems del menú...');
  const batch = db.batch();

  items.forEach((item) => {
    const { id, ...data } = item;
    const ref = db.collection('items').doc(id);
    batch.set(ref, { ...data, synced: true });
  });

  await batch.commit();
  console.log(`✅ ${items.length} ítems insertados.`);
}

async function createAdminUser(uid, firstName, lastName) {
  console.log(`👤 Creando usuario admin: ${firstName} ${lastName}`);
  await db.collection('users').doc(uid).set({
    firstName,
    lastName,
    phone:        '',
    profilePhoto: '',
    role:         'ADMIN',
    address:      '',
    createdAt:    admin.firestore.FieldValue.serverTimestamp(),
  });
  console.log('✅ Usuario admin creado.');
}

// ─── Main ─────────────────────────────────────────────────────────────────────

async function main() {
  try {
    await seedItems();

    // Opcional: crear un admin inicial.
    // Reemplazá el UID con el UID real del usuario en Firebase Auth.
    // await createAdminUser('UID_DEL_ADMIN_AQUI', 'Admin', 'ExpressFood');

    console.log('\n🎉 Seed completado exitosamente.');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error en el seed:', error);
    process.exit(1);
  }
}

main();
