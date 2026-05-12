const { Client } = require('pg');

const connectionString = "postgresql://postgres:UZPDcsRM31QpC6VN@db.onnfnjbihkuqdsfsrxqw.supabase.co:5432/postgres";

const client = new Client({
  connectionString: connectionString,
});

async function fixIdentities() {
  try {
    await client.connect();
    console.log("Connected to Supabase. Creating missing identities for all users...");

    // 1. Get the users who are missing identities
    const res = await client.query(`
      SELECT id, email 
      FROM auth.users 
      WHERE id NOT IN (SELECT user_id FROM auth.identities)
      AND email != 'admin@example.com'
    `);
    
    console.log(`Found ${res.rows.length} users missing identities.`);

    for (const row of res.rows) {
      console.log(`Creating identity for ${row.email}...`);
      
      // We need to generate a unique ID for the identity (can be UUID)
      // And link it to the user. Provider is 'email'.
      await client.query(`
        INSERT INTO auth.identities (id, user_id, identity_data, provider, last_sign_in_at, created_at, updated_at)
        VALUES (
          gen_random_uuid(),
          $1,
          jsonb_build_object('sub', $1, 'email', $2),
          'email',
          NOW(),
          NOW(),
          NOW()
        )
      `, [row.id, row.email]);
    }
    
    console.log("✅ All identities created. The users are now complete and authenticatable.");

  } catch (err) {
    console.error("Error fixing identities:", err.message);
  } finally {
    await client.end();
  }
}

fixIdentities();
