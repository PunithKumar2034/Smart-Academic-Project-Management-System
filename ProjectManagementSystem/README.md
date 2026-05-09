# Smart Academic Project Management System

## Overview
A modern, animated, and scalable Java desktop application for academic project management. Built with **JavaFX** (frontend) and **Supabase PostgreSQL** (backend). It features role-based access control (Admin, Faculty, Student) and sophisticated team formation logic.

## Architecture & Folder Structure

The repository enforces a strict separation of concerns to guarantee maintainability and security:

```
ProjectManagementSystem/
├── frontend/                   # UI logic, JavaFX screens, models, controllers
│   ├── src/main/java/com/pms/
│   │   ├── app/                # Main application entry points
│   │   ├── controllers/        # FXML view controllers
│   │   ├── models/             # Local data structures
│   │   ├── services/           # Supabase REST integration logic
│   │   └── utils/              # Helper functions, config loaders
│   └── src/main/resources/
│       ├── views/              # FXML layouts
│       ├── css/                # Dark theme stylesheets
│       └── images/             # Icons & assets
├── backend/                    # Supabase database artifacts
│   ├── sql/
│   │   ├── schema.sql          # Base PostgreSQL tables and relationships
│   │   ├── seed.sql            # Initial test data
│   │   └── procedures.sql      # Triggers and SQL functions
├── config/                     # Configuration files (Git-ignored)
│   └── auth.json               # Supabase API keys & config
├── pom.xml                     # Maven dependencies (JavaFX, JMetro, Jackson)
└── README.md                   # Project documentation
```

### Layer Explanation
1. **Frontend (JavaFX):** Handles all visual interactions, animations (using AnimateFX), and routing. It operates entirely via REST API calls to Supabase, containing no direct database connection strings.
2. **Backend (Supabase):** Manages data persistence, role-level security (RLS), and authentication. The logic is primarily housed in PostgreSQL, enabling robust scalability.
3. **Configuration:** Separated into a dedicated `config/` directory. `auth.json` is strictly added to `.gitignore` to prevent secret leakage.

## Tech Stack
- **Language:** Java 21
- **UI Framework:** JavaFX
- **UI Themes:** JMetro (Dark Mode) + Custom CSS + AnimateFX
- **Backend:** Supabase (PostgreSQL, GoTrue Auth, REST Auto-API)
- **Build Tool:** Maven

## Getting Started

1. **Setup Supabase:** Create a new project on [Supabase](https://supabase.com). Run `backend/sql/schema.sql` in the SQL Editor.
2. **Configure Auth:** Copy your Supabase URL and Anon Key into `config/auth.json`.
3. **Run Application:** Use Maven to run the JavaFX application.
   ```bash
   mvn clean javafx:run
   ```

## Development Phases
- **Phase 1:** Core structure, dependencies, initial UI, and base database schema. *(Current)*
- **Phase 2:** Authentication REST integration and role-routing.
- **Phase 3:** Admin/Faculty dashboards and subject/project management.
- **Phase 4:** Student dashboard, smart team allocation, and FIFO project logic.
- **Phase 5:** Final animations, deadline reminders, and notification system.
