-- Note: You should only run this AFTER your admin@example.com user exists in the public.users table.
-- If the trigger wasn't active when you made the admin user, run this manually:
-- INSERT INTO users (id, name, email, role) VALUES ('<YOUR_ADMIN_UUID_HERE>', 'Admin', 'admin@example.com', 'admin');

-- Insert Initial Subjects
INSERT INTO subjects (subject_name, description) VALUES
('Database Management Systems', 'Core concepts of relational databases, SQL, and normalization.'),
('Software Engineering', 'Agile methodologies, SDLC, and project management.'),
('Machine Learning', 'Introduction to supervised and unsupervised learning algorithms.')
ON CONFLICT (subject_name) DO NOTHING;

-- Insert Mock Faculty (Assuming we have a faculty ID, but since UUIDs are dynamic, we will use a DO block or just leave this for the UI)
-- The best way to seed relational UUID data is through the Java UI you're building, but these subjects are a great start!
