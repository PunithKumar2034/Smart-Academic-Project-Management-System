const { Client } = require('pg');

// Using the IPv6 address in brackets
const connectionString = "postgresql://postgres:UZPDcsRM31QpC6VN@[2406:da1a:314:7101:f271:e80c:e9ea:4f9e]:5432/postgres";

const client = new Client({
  connectionString: connectionString,
});

async function fixAll() {
  try {
    await client.connect();
    console.log("Connected to Supabase. Re-syncing users and identities...");

    const sql = `
      -- 1. Ensure all users have the correct instance_id
      UPDATE auth.users 
      SET instance_id = '00000000-0000-0000-0000-000000000000'
      WHERE email LIKE '%@example.com';

      -- 2. Create missing identities
      INSERT INTO auth.identities (id, user_id, identity_data, provider, last_sign_in_at, created_at, updated_at)
      SELECT 
        gen_random_uuid(), 
        id, 
        jsonb_build_object('sub', id, 'email', email), 
        'email', 
        NOW(), 
        NOW(), 
        NOW()
      FROM auth.users 
      WHERE id NOT IN (SELECT user_id FROM auth.identities)
      AND email LIKE '%@example.com';

      -- 3. Reset passwords one more time to be safe
      UPDATE auth.users 
      SET encrypted_password = (SELECT encrypted_password FROM auth.users WHERE email = 'admin@example.com')
      WHERE email LIKE '%@example.com' AND email != 'admin@example.com';
    `;

    const res = await client.query(sql);
    console.log("✅ All users fixed and identities created.");

  } catch (err) {
    console.error("Error fixing DB:", err.message);
  } finally {
    await client.end();
  }
}

fixAll();
