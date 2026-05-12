const https = require('https');

const supabaseUrl = "https://onnfnjbihkuqdsfsrxqw.supabase.co";
const supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9ubmZuamJpaGt1cWRzZnNyeHF3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzNDA1NTYsImV4cCI6MjA5MzkxNjU1Nn0.564RC6hlay3GYe7YApk4GtHpvdT5Gc2yg0jutMG4kY4";

const users = [
  "smith@example.com", "jones@example.com", "brown@example.com",
  "student1@example.com", "student2@example.com", "student3@example.com",
  "student4@example.com", "student5@example.com", "student6@example.com",
  "student7@example.com", "student8@example.com", "student9@example.com",
  "student10@example.com"
];

async function signup(email) {
  return new Promise((resolve) => {
    const data = JSON.stringify({
      email: email,
      password: "Password123!"
    });

    const options = {
      hostname: 'onnfnjbihkuqdsfsrxqw.supabase.co',
      port: 4443, // Trying 4443 or default 443
      path: '/auth/v1/signup',
      method: 'POST',
      headers: {
        'apikey': supabaseKey,
        'Content-Type': 'application/json',
        'Content-Length': data.length
      }
    };
    
    // If 4443 fails, fallback to 443
    const req = https.request({ ...options, port: 443 }, (res) => {
      let body = '';
      res.on('data', (d) => body += d);
      res.on('end', () => {
        console.log(`Signup ${email}: ${res.statusCode}`);
        resolve();
      });
    });

    req.on('error', (e) => {
      console.error(`Error ${email}: ${e.message}`);
      resolve();
    });

    req.write(data);
    req.end();
  });
}

async function run() {
  console.log("Starting official signups...");
  for (const email of users) {
    await signup(email);
    // Add small delay to avoid rate limits
    await new Promise(r => setTimeout(r, 500));
  }
  console.log("Done. Now we need to confirm them via SQL.");
}

run();
