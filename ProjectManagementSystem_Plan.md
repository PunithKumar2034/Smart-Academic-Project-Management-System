# Smart Academic Project & Team Management System
## Phase-by-Phase Development Plan for Antigravity + Supabase + Java Desktop

### Project Goal
Build a secure, role-based, animated desktop application for academic project management. The system will support Admin, Faculty, and Student users. Admin is a special Faculty account detected by email and granted elevated privileges. Faculty can manage students, projects, and subject mappings. Students can browse projects, form teams, and receive deadline reminders. The backend will use Supabase, while the frontend will be a Java desktop app with a modern dark UI and interactive animations.

---

## 1) Repository and File Arrangement

### Recommended folder structure
```text
ProjectManagementSystem/
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   ├── ui/
│   │   ├── components/
│   │   ├── animations/
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── services/
│   │   └── utils/
│   ├── assets/
│   │   ├── icons/
│   │   ├── images/
│   │   └── themes/
│   └── build/
│
├── backend/
│   ├── sql/
│   │   ├── schema.sql
│   │   ├── seed.sql
│   │   ├── triggers.sql
│   │   └── procedures.sql
│   ├── auth/
│   │   ├── auth_service.*
│   │   ├── role_service.*
│   │   └── session_service.*
│   ├── supabase/
│   │   ├── queries/
│   │   ├── repositories/
│   │   └── policies/
│   └── logic/
│       ├── team_allocator.*
│       ├── notification_service.*
│       └── validation.*
│
├── config/
│   ├── auth.json
│   └── app.config.json
│
├── .gitignore
├── README.md
└── AntigravityPrompt.md
```

### File arrangement rules
- Put all **Supabase SQL**, **role logic**, **auth logic**, and **data queries** under `backend/`.
- Put all **Java UI code**, **screen classes**, **components**, **animations**, and **theme files** under `frontend/`.
- Store all sensitive keys, IDs, and auth values in `config/auth.json`.
- Add `config/auth.json` to `.gitignore` so GitHub never receives secrets.
- Keep one folder for each concern: UI, database, auth, logic, and configuration.

### Suggested `.gitignore`
```gitignore
/build
/out
/.idea
*.log
config/auth.json
config/*.local.json
frontend/build
backend/.cache
```

---

## 2) Professional Backend Design

### 2.1 Core user roles
The system will support three user roles:
- **Admin**: A faculty account with full system control.
- **Faculty**: Manages students, subjects, and projects.
- **Student**: Selects projects, joins teams, and receives reminders.

### 2.2 Better admin model
Instead of a separate admin login page, use one faculty login flow and detect admin access after authentication.

#### Recommended logic
- All users log in through the same faculty-style login flow.
- After login, the system checks:
  - email match against a preconfigured admin email list, or
  - `is_admin = true` flag in user profile.
- If matched, the app loads the Admin dashboard.
- Otherwise, it loads the Faculty dashboard.

### 2.3 Improved permissions
#### Admin can:
- Add faculty
- Add or edit subjects
- Assign faculty to subjects
- Reassign subjects when needed
- View all projects, teams, and notifications
- Approve or disable faculty/project mappings

#### Faculty can:
- Add students to their supervision list
- Create projects for their subject
- Define team size and project capacity
- Manage student applications
- View project progress
- Send deadline alerts

#### Student can:
- View available projects
- Apply for a project
- Join a team
- See assigned team members
- Receive deadline notifications
- View project progress

---

## 3) Database Design for Supabase

### 3.1 Design improvement
Use the same conceptual schema you already designed, but make it more practical for Supabase:
- Use `profiles` or `users` table for user identity and role.
- Use `subjects`, `faculty_subjects`, `projects`, `teams`, `team_members`, `applications`, `deadlines`, and `notifications`.
- Keep a clean separation between authentication and profile data.

### 3.2 Suggested tables

#### `users`
Stores profile information for all users.
- `id`
- `name`
- `email`
- `role` (`admin`, `faculty`, `student`)
- `is_active`
- `created_at`

#### `subjects`
Stores academic subjects.
- `id`
- `subject_name`
- `description`
- `created_by`
- `updated_by`
- `created_at`

#### `faculty_subjects`
Maps faculty to subjects.
- `id`
- `faculty_id`
- `subject_id`
- `assigned_at`

#### `projects`
Stores projects offered by a faculty/subject.
- `id`
- `subject_id`
- `faculty_id`
- `project_name`
- `description`
- `max_team_size`
- `selection_mode`
- `status`
- `created_at`

#### `project_applications`
Stores student applications for projects.
- `id`
- `project_id`
- `student_id`
- `applied_at`
- `status`

#### `teams`
Stores formed teams.
- `id`
- `project_id`
- `team_name`
- `status`
- `created_at`

#### `team_members`
Stores members of each team.
- `team_id`
- `student_id`
- `joined_at`
- Primary key: `(team_id, student_id)`

#### `deadlines`
Stores milestones and reminder dates.
- `id`
- `project_id`
- `title`
- `due_date`
- `reminder_days_before`
- `created_at`

#### `notifications`
Stores alerts for users.
- `id`
- `user_id`
- `type`
- `message`
- `is_read`
- `created_at`

---

## 4) Team Creation Logic

### Option A: Smart allocation by strength
Students choose a project, and the system auto-balances team composition based on:
- team strength
- number of applicants
- remaining capacity
- fairness in assignment

#### Best use case
When you want the project to feel intelligent and algorithmic.

### Option B: FIFO project locking
Students apply first-come-first-serve.
- Once the project capacity is full, new students cannot join.
- Remaining students must choose another project.

#### Best use case
When simplicity and strict capacity control matter.

### Professional improvement
Do not force both modes at the same time. Use a field like:
- `selection_mode = smart`
- `selection_mode = fifo`

This makes the system cleaner and easier to explain.

---

## 5) Backend Logic Modules

### `auth_service`
Handles:
- login
- logout
- session validation
- role detection
- admin email check

### `role_service`
Handles:
- permission checks
- dashboard routing
- access control for each feature

### `team_allocator`
Handles:
- smart assignment
- FIFO assignment
- team balancing
- project capacity validation

### `notification_service`
Handles:
- deadline reminders
- project alerts
- unread notification count
- popup generation for frontend

### `validation`
Handles:
- duplicate project checks
- max capacity checks
- invalid subject assignment
- role mismatch errors

---

## 6) Frontend GUI Plan

### 6.1 Recommended Java technology
Use **JavaFX** for the desktop UI because it supports:
- modern layouts
- animation control
- custom styling
- better-looking components than basic Swing

### 6.2 Suggested UI style
- Dark theme
- Soft gradient backgrounds
- Floating cards
- Wave-like header or background motion
- Smooth page transitions
- Hover effects on buttons and cards
- Rounded corners
- Glass-morphism style panels where appropriate

### 6.3 Helpful UI libraries
- **JMetro** for fluent-style dark theme
- **AnimateFX** for transitions and motion
- Optional custom CSS for deeper styling

### 6.4 Layout design
#### Top area
- App title on the left
- Notification icon
- Circular user avatar on the top right

#### Left sidebar
- Dashboard
- Subjects
- Projects
- Teams
- Students
- Notifications
- Settings
- Admin tools when logged in as admin

#### Avatar behavior
When clicked, show a small drop-down panel with:
- user name
- email
- role
- logout button

### 6.5 Role-based UI
#### Admin dashboard
- Add faculty
- Add subject
- Assign faculty to subject
- Reassign faculty
- View system reports
- Manage all projects

#### Faculty dashboard
- Add students
- Create project
- Set max team size
- View applicants
- Form teams
- Send reminders

#### Student dashboard
- View available projects
- Apply for project
- View team
- See deadline reminders
- Check notifications

---

## 7) Security and Robustness

### Recommended improvements
- Do not store passwords as plain text.
- Use Supabase Auth or hashed credential handling.
- Keep all sensitive keys in `auth.json`.
- Use encapsulation in Java classes:
  - private fields
  - public getters/setters
  - validation inside service classes
- Separate UI and business logic properly.
- Never hardcode secrets inside Java files.

### `auth.json` example
```json
{
  "supabaseUrl": "YOUR_SUPABASE_URL",
  "supabaseAnonKey": "YOUR_ANON_KEY",
  "adminEmails": ["admin@example.com"]
}
```

---

## 8) Phase-by-Phase Development Plan

### Phase 1: Planning and setup
- Finalize folder structure
- Create GitHub repository
- Add `.gitignore`
- Create `auth.json`
- Prepare `README.md`

### Phase 2: Database foundation
- Create schema
- Insert seed data
- Add constraints and relationships
- Write initial queries

### Phase 3: Auth and role logic
- Implement login
- Detect admin email
- Route users to correct dashboard
- Enforce role permissions

### Phase 4: Core backend logic
- Project creation
- Subject management
- Team allocation
- Notifications
- Deadline reminders

### Phase 5: Frontend UI
- Build JavaFX screens
- Add sidebar
- Add top-right profile avatar
- Add dark theme
- Add animations and transitions

### Phase 6: Integration
- Connect Java frontend to Supabase
- Test CRUD operations
- Test login and role routing
- Test project selection and team flow

### Phase 7: Testing and polish
- Fix UI issues
- Prevent duplicate project applications
- Validate team limits
- Test notifications
- Improve responsiveness

### Phase 8: Final documentation
- Add screenshots
- Add ER diagram
- Add relational schema
- Add module explanation
- Add future scope

---

## 9) Best Extra Ideas to Make the Project Stand Out

### Idea 1: Smart deadline predictor
Show a warning when a project deadline is close and team progress is low.

### Idea 2: Progress indicator
Display project completion as a percentage with animated progress bars.

### Idea 3: Auto notifications
Send notification when:
- a student joins a project
- a project is full
- a deadline is near
- faculty changes a project

### Idea 4: Project health score
Show whether a project is:
- on track
- at risk
- delayed

### Idea 5: Search and filter
Add filters for:
- subject
- faculty
- project status
- team size
- deadline range

---

## 10) Final Project Statement

This project is a role-based academic project management desktop application built with a Java frontend and Supabase backend. It supports admin, faculty, and student workflows, secure authentication, project allocation, team formation, deadline reminders, and a visually modern animated interface. The architecture is separated cleanly into frontend, backend, and config layers for maintainability, security, and easy Git-based collaboration.


---

## 11) Best Extra Ideas to Make the Project Stand Out

### Idea 1: Smart deadline predictor
Show a warning when a project deadline is close and team progress is low.

### Idea 2: Progress indicator
Display project completion as a percentage with animated progress bars.

### Idea 3: Auto notifications
Send notification when:
- a student joins a project
- a project is full
- a deadline is near
- faculty changes a project

### Idea 4: Project health score
Show whether a project is:
- on track
- at risk
- delayed

### Idea 5: Search and filter
Add filters for:
- subject
- faculty
- project status
- team size
- deadline range

---

## 12) Phase-by-Phase Development Plan

### Phase 1: Planning and setup
- Finalize folder structure
- Create GitHub repository
- Add `.gitignore`
- Create `auth.json`
- Prepare `README.md`

### Phase 2: Database foundation
- Create schema
- Insert seed data
- Add constraints and relationships
- Write initial queries

### Phase 3: Auth and role logic
- Implement login
- Detect admin email
- Route users to correct dashboard
- Enforce role permissions

### Phase 4: Core backend logic
- Project creation
- Subject management
- Team allocation
- Notifications
- Deadline reminders

### Phase 5: Frontend UI
- Build JavaFX screens
- Add sidebar
- Add top-right profile avatar
- Add dark theme
- Add animations and transitions

### Phase 6: Integration
- Connect Java frontend to Supabase
- Test CRUD operations
- Test login and role routing
- Test project selection and team flow

### Phase 7: Testing and polish
- Fix UI issues
- Prevent duplicate project applications
- Validate team limits
- Test notifications
- Improve responsiveness

### Phase 8: Final documentation
- Add screenshots
- Add ER diagram
- Add relational schema
- Add module explanation
- Add future scope

---


### Project Plan (Phase-by-Phase)
Below is a detailed phase-by-phase plan for the project, covering file structure, Supabase backend setup, Java desktop frontend design, and integration steps. The goal is a well-structured repository and a robust, interactive application.

Phase 1: Repository Setup & File Structure
Create the project repository (on GitHub or similar) with a clear root directory.
Folder layout:
graphql
Copy
ProjectName/
├── backend/        # Supabase queries, SQL scripts, and backend logic
├── frontend/       # Java source code and UI assets
├── config/         # Configuration files (e.g., auth.json)
├── auth.json       # (Keys and IDs – add this to .gitignore)
├── .gitignore      # Ignore auth.json and other sensitive files
└── README.md       # Project documentation
backend/ will hold all Supabase-related code:
SQL schema scripts: e.g. schema.sql, and any initial data inserts.
Auth and logic files: e.g. classes or scripts for Supabase queries, role checks, etc.
frontend/ will hold the Java application:
Source code folders: e.g. src/ with packages like ui/, models/, controllers/.
Assets: e.g. assets/ with images, icons, CSS (for JavaFX), etc.
auth.json: Store Supabase project URL and keys (as well as any API secrets) here, and add it to .gitignore so that sensitive credentials are not checked into Git.
Phase 2: Supabase Setup & Database Schema
Create a Supabase project: This provides a dedicated PostgreSQL database with built-in Auth and real-time features
. Supabase automatically generates RESTful APIs from your tables, so we "never write an API again" for basic CRUD
.
Design tables (Postgres schema): Mirror and extend the earlier schema. For example:
sql
Copy
-- Users table with roles
CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  name          TEXT NOT NULL,
  email         TEXT UNIQUE NOT NULL,
  password      TEXT NOT NULL,
  role          VARCHAR(10) NOT NULL -- 'admin', 'faculty', or 'student'
);

-- Subjects (courses or topics)
CREATE TABLE subjects (
  id            SERIAL PRIMARY KEY,
  name          TEXT UNIQUE NOT NULL,
  description   TEXT
);

-- Many-to-many: which faculty teach which subjects
CREATE TABLE faculty_subject (
  faculty_id    INTEGER REFERENCES users(id),
  subject_id    INTEGER REFERENCES subjects(id),
  PRIMARY KEY (faculty_id, subject_id)
);

-- Projects (offered by a subject)
CREATE TABLE projects (
  id            SERIAL PRIMARY KEY,
  name          TEXT NOT NULL,
  subject_id    INTEGER REFERENCES subjects(id),
  max_team_size INTEGER NOT NULL,
  description   TEXT
);

-- Teams and team members
CREATE TABLE teams (
  id            SERIAL PRIMARY KEY,
  project_id    INTEGER REFERENCES projects(id)
);
CREATE TABLE team_members (
  team_id       INTEGER REFERENCES teams(id),
  student_id    INTEGER REFERENCES users(id),
  PRIMARY KEY (team_id, student_id)
);
Roles: The users.role field distinguishes admin, faculty, and student. (Alternatively, you could maintain a separate admin list by email, but storing roles in the DB is more straightforward.)
Admin user: Insert one initial admin user (role='admin') into users. For example:
sql
Copy
INSERT INTO users (name, email, password, role)
VALUES ('Admin Name', 'admin@example.com', '<hashed_password>', 'admin');
Additional tables: You may include tables for notifications or deadlines if needed (e.g. a deadlines table for project milestones). Supabase supports Postgres extensions like pgcrypto for hashing (passwords) or JWT and Row-Level Security (RLS) for access control
.
Initial data: Seed the database with at least one faculty user, one subject, and perhaps a couple of example projects. Ensure the admin’s email is recognized (system can check email or use role to detect admin).
Phase 3: Backend Logic & Auth Flows
Supabase Auth: Enable email/password authentication in Supabase. After login, fetch the user’s profile (including role) from the database.
Admin logic:
When an admin logs in (detect via email or role='admin'), show admin interface.
Admin can add new faculty (create a new users entry with role='faculty') and add/assign subjects. For example:
Add subject: insert into subjects.
Assign faculty: insert into faculty_subject (to link a faculty to a subject).
Faculty logic:
Faculty users can add new students: create users entries with role='student' and store which faculty they belong to (e.g. in a faculty_subject or a supervisor_id column).
Faculty can create new projects for their subject: insert into projects with subject_id.
Student logic (team creation): Two options as described:
Automated team distribution: Students select a project; an algorithm (running on the backend) periodically groups students into teams of size = project’s max_team_size. If some students remain unmatched, the system can auto-assign them to projects with available slots (balancing team sizes).
First-come-first-serve: Each project allows up to its max team size; once full, no more students can join. New applicants must pick a different project.
Choose one strategy (or implement both and allow mode toggle).
Notifications & deadlines:
Add a deadlines table (e.g. with fields project_id, milestone_name, due_date).
Use Supabase’s Postgres triggers or schedule functions: when a deadline is near or passed, insert records into a notifications table for affected users. Use Supabase’s real-time or serverless functions to trigger reminder notifications.
Security: Use Postgres Row-Level Security (RLS) so that faculty only see their own students/projects, and students only see projects they belong to
. JWT auth ensures only logged-in users access data.
Supabase API calls: In Java, use HTTP/REST or a Supabase Java library (via generated API) to perform these operations. Supabase “introspects your database and provides instant APIs,” so every table is immediately accessible via REST
.
Phase 4: Frontend UI Design (Java Desktop)
Technology: Use JavaFX for a modern desktop UI (cleaner animation support than Swing). Enhance with libraries:
AnimateFX: A library of “+70 ready-to-use animations for JavaFX”
, to add smooth transitions (e.g. bouncing buttons, fade-ins).
JMetro or JFoenix: Provide sleek dark/light themes and material-design components. JMetro offers a polished Fluent-inspired theme with built-in animations
.
Theme: Dark mode by default (professional look). Use CSS or library theming to switch between light/dark if needed.
Layout:
Main window: Split into a left sidebar (navigation) and a main content area. The window background can have subtle “wave” or gradient animations for flair.
Sidebar: Contains navigation buttons (with icons) for Dashboard, Manage Subjects, Manage Users, Projects, etc. These change based on role:
Admin sidebar: Options for “Add Faculty”, “Manage Subjects”, “View Reports”, etc.
Faculty sidebar: Options for “Add Student”, “My Subjects”, “Create Project”, etc.
Student sidebar: Options for “Available Projects”, “My Team”, “Notifications”, etc.
Top bar / User info: In the top-right, show a circular avatar or user icon. Clicking it opens a dropdown/popup tab with user details (name, role, email) and a Logout button.
Interactive elements:
Buttons and cards should animate on hover/click (e.g. a slight bounce or color shift via AnimateFX).
Lists/tables of data (projects, students) should load with fade-in or slide animations.
Use encapsulation in code: define classes like User, Project, etc., with private fields and getters/setters. Encrypt any sensitive data in transit (JavaFX over HTTPS) and rely on Supabase’s secure auth.
Notifications:
Display deadline reminders (e.g. a blinking icon or banner) when projects’ deadlines approach.
Could show a modal or toast popup for urgent alerts.
Responsive design: Though it’s desktop-only, ensure the window resizes gracefully; controls should be large enough for touch if needed.
Phase 5: Integration, Testing & Deployment
Connect frontend to Supabase: Use Java HTTP clients to call the Supabase REST endpoints (for auth, queries, inserts). Store the Supabase URL/key in auth.json.
Test user flows:
Admin: Log in as admin → add a faculty → add subject → verify in DB.
Faculty: Log in as faculty → add students and projects → ensure data appears.
Student: Register/log in as student → select a project → test team assignment logic.
Test role detection (admin vs. faculty) by email or role field.
Error handling: Add checks for invalid operations (e.g. prevent overfilling a project team, show errors if subject names clash, etc.).
Finalize: Package the app (e.g. into a runnable JAR or installer). Ensure .gitignore excludes auth.json and any build artifacts.
Documentation: In README.md, include setup instructions (e.g. how to configure Supabase, add admin email, run SQL schema, compile and run the app).
This phased plan yields a clean architecture: a frontend folder with all Java UI code (leveraging animations and themes), and a backend folder handling all Supabase interactions (database schema, auth flows, queries). Sensitive keys live in auth.json (gitignored) for safety. By phase-by-phase development, you ensure each part (file setup, database, API logic, UI) is well-designed and integrated.

Sources: Supabase provides a managed Postgres database with auto-generated APIs
, and JavaFX libraries like JMetro and AnimateFX offer dark themes and ready animations
 for a polished desktop UI.

## 10) Final Project Statement

This project is a role-based academic project management desktop application built with a Java frontend and Supabase backend. It supports admin, faculty, and student workflows, secure authentication, project allocation, team formation, deadline reminders, and a visually modern animated interface. The architecture is separated cleanly into frontend, backend, and config layers for maintainability, security, and easy Git-based collaboration.
