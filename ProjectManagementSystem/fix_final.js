const { Client } = require('pg');

// Using the Supavisor pooler hostname which is IPv4 compatible
const connectionString = "postgresql://postgres.onnfnjbihkuqdsfsrxqw:UZPDcsRM31QpC6VN@aws-0-ap-south-1.pooler.supabase.com:5432/postgres";

const client = new Client({
  connectionString: connectionString,
});

async function fixFinal() {
  try {
    await client.connect();
    console.log("Connected to Supabase via Pooler. Fixing users and identities...");

    const sql = `
      -- 1. Ensure all users have the correct instance_id
      UPDATE auth.users 
      SET instance_id = '00000000-0000-0000-0000-000000000000'
      WHERE email LIKE '%@example.com';

      -- 2. Delete existing identities for these users to start clean
      DELETE FROM auth.identities 
      WHERE user_id IN (SELECT id FROM auth.users WHERE email LIKE '%@example.com' AND email != 'admin@example.com');

      -- 3. Create fresh identities
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
      WHERE email LIKE '%@example.com' AND email != 'admin@example.com';

      -- 4. Reset passwords one more time
      UPDATE auth.users 
      SET encrypted_password = (SELECT encrypted_password FROM auth.users WHERE email = 'admin@example.com')
      WHERE email LIKE '%@example.com' AND email != 'admin@example.com';
    `;

    await client.query(sql);
    console.log("✅ All users fixed and identities recreated.");

  } catch (err) {
    console.error("Error fixing DB:", err.message);
  } finally {
    await client.end();
  }
}

fixFinal();
