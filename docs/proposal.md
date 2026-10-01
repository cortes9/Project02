# Study Group Finder API Proposal

## 1. The pitch (one paragraph)

The Study Group Finder API helps college students discover, create, and join study sessions based on courses, buildings, times, and available capacity. Students can browse sessions, filter by course or location, create their own sessions, and join or leave sessions. The Android client needs this API to provide authenticated, shared, and persistent study-group data instead of storing sessions only on individual devices.

## 2. Resources

| Resource | Key fields | Relationships |
|---|---|---|
| User | user_id, oauth_subject, name, email, student_year, major_id, is_admin | A User creates study sessions and joins many study sessions |
| Major | major_id, major_name | A Major is associated with many users and study sessions |
| Course | course_id, course_code, subject, course_name | A Course has many study sessions |
| Building | building_id, building_name, room_number | A Building can host many study sessions |
| Study Session | session_id, creator_id, course_id, building_id, delivery_mode, start_time, end_time, capacity, description, status | A Study Session belongs to one creator and course and has many members |
| Session Member | user_id, session_id, joined_at | Connects Users and Study Sessions through a many-to-many relationship |
| Session Major | session_id, major_id | Connects Study Sessions and Majors through a many-to-many relationship |

## 3. ER sketch

Tables, primary and foreign keys, and cardinality. Edit this Mermaid diagram (it renders on GitHub; try changes at https://mermaid.live):

```mermaid
erDiagram
    USER ||--o{ STUDY_SESSION : creates
    USER ||--o{ SESSION_MEMBER : joins
    STUDY_SESSION ||--o{ SESSION_MEMBER : has
    STUDY_SESSION }o--|| COURSE : focuses_on
    STUDY_SESSION }o--o| BUILDING : held_at
    STUDY_SESSION ||--o{ SESSION_MAJOR : targets
    MAJOR ||--o{ SESSION_MAJOR : applies_to
    USER }o--|| MAJOR : studies

    USER {
        bigint user_id PK
        string oauth_subject UK
        string name
        string email UK
        string student_year
        bigint major_id FK
        boolean is_admin
    }

    MAJOR {
        bigint major_id PK
        string major_name UK
    }

    COURSE {
        bigint course_id PK
        string course_code UK
        string subject
        string course_name
    }

    BUILDING {
        bigint building_id PK
        string building_name
        string room_number
    }

    STUDY_SESSION {
        bigint session_id PK
        bigint creator_id FK
        bigint course_id FK
        bigint building_id FK "nullable"
        string delivery_mode
        datetime start_time
        datetime end_time
        int capacity
        string description "nullable"
        string status
    }

    SESSION_MEMBER {
        bigint user_id PK, FK
        bigint session_id PK, FK
        datetime joined_at
    }

    SESSION_MAJOR {
        bigint session_id PK, FK
        bigint major_id PK, FK
    }

```markdown
# Study Group Finder API Proposal

## 1. The pitch (one paragraph)

The Study Group Finder API helps college students discover, create, and join study sessions based on courses, buildings, times, and available capacity. Students can browse sessions, filter by course or location, create their own sessions, and join or leave sessions. The Android client needs this API to provide authenticated, shared, and persistent study-group data instead of storing sessions only on individual devices.

## 2. Resources

| Resource | Key fields | Relationships |
|---|---|---|
| User | user_id, oauth_subject, name, email, student_year, major_id, is_admin | A User creates study sessions and joins many study sessions |
| Major | major_id, major_name | A Major is associated with many users and study sessions |
| Course | course_id, course_code, subject, course_name | A Course has many study sessions |
| Building | building_id, building_name, room_number | A Building can host many study sessions |
| Study Session | session_id, creator_id, course_id, building_id, delivery_mode, start_time, end_time, capacity, description, status | A Study Session belongs to one creator and course and has many members |
| Session Member | user_id, session_id, joined_at | Connects Users and Study Sessions through a many-to-many relationship |
| Session Major | session_id, major_id | Connects Study Sessions and Majors through a many-to-many relationship |

## 3. ER sketch

Tables, primary and foreign keys, and cardinality. Edit this Mermaid diagram (it renders on GitHub; try changes at https://mermaid.live):

```mermaid
erDiagram
    USER ||--o{ STUDY_SESSION : creates
    USER ||--o{ SESSION_MEMBER : joins
    STUDY_SESSION ||--o{ SESSION_MEMBER : has
    STUDY_SESSION }o--|| COURSE : focuses_on
    STUDY_SESSION }o--o| BUILDING : held_at
    STUDY_SESSION ||--o{ SESSION_MAJOR : targets
    MAJOR ||--o{ SESSION_MAJOR : applies_to
    USER }o--|| MAJOR : studies

    USER {
        bigint user_id PK
        string oauth_subject UK
        string name
        string email UK
        string student_year
        bigint major_id FK
        boolean is_admin
    }

    MAJOR {
        bigint major_id PK
        string major_name UK
    }

    COURSE {
        bigint course_id PK
        string course_code UK
        string subject
        string course_name
    }

    BUILDING {
        bigint building_id PK
        string building_name
        string room_number
    }

    STUDY_SESSION {
        bigint session_id PK
        bigint creator_id FK
        bigint course_id FK
        bigint building_id FK "nullable"
        string delivery_mode
        datetime start_time
        datetime end_time
        int capacity
        string description "nullable"
        string status
    }

    SESSION_MEMBER {
        bigint user_id PK, FK
        bigint session_id PK, FK
        datetime joined_at
    }

    SESSION_MAJOR {
        bigint session_id PK, FK
        bigint major_id PK, FK
    }
```

## 4. Endpoints

| Verb | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/v1/study-sessions?page=0&size=20&sort=startTime,asc` | public | List study sessions with pagination and sorting |
| POST | `/api/v1/study-sessions` | user | Create a study session |
| GET | `/api/v1/study-sessions/{sessionId}` | public | View one study session |
| PUT | `/api/v1/study-sessions/{sessionId}` | user | Completely replace a study session owned by the current user |
| PATCH | `/api/v1/study-sessions/{sessionId}` | user | Partially update a study session owned by the current user |
| DELETE | `/api/v1/study-sessions/{sessionId}` | user | Delete a study session owned by the current user |
| POST | `/api/v1/study-sessions/{sessionId}/join` | user | Join a study session if capacity is available |
| DELETE | `/api/v1/study-sessions/{sessionId}/leave` | user | Leave a study session |
| GET | `/api/v1/courses?subject=CS` | public | List courses with optional subject filtering |
| GET | `/api/v1/buildings` | public | List available buildings |
| DELETE | `/api/v1/users/me` | user | Delete the current user's account and associated data |
| GET | `/api/v1/users?page=0&size=20` | admin | List users with pagination |
| GET | `/api/v1/users/{userId}` | admin | View one user's information |
| PATCH | `/api/v1/users/{userId}` | admin | Update a user's administrative status |
| DELETE | `/api/v1/users/{userId}` | admin | Delete a user and associated data |

The study-session and user collections support pagination. Study sessions can be filtered by course, building, delivery mode, status, and time range. Collections can be sorted by fields such as `startTime`.

Expected error responses include:

- `400 Bad Request` for malformed or invalid input
- `401 Unauthorized` when authentication is missing or invalid
- `403 Forbidden` when the authenticated user lacks permission
- `404 Not Found` when a resource does not exist
- `409 Conflict` when a session is full or a user is already a member
- `500 Internal Server Error` for unexpected server failures

Errors will use RFC 9457 Problem Details with the `application/problem+json` content type.

## 5. Technical choices


## 6. Risks

1. **OAuth configuration may delay development.**
   The team will validate OAuth with PKCE and API token validation early, before building dependent features. The API must return `401` for missing or invalid tokens.

2. **Hosted database access or deployment may not work for every teammate.**
   The team will test the shared hosted database connection, environment-variable setup, and teammate access during Sprint 1. No database credentials will be committed to the repository.

## 7. Team and Sprint 1

- **Julian:** Complete `docs/proposal.md` and submit the proposal pull request.
- **Jorman:** Finalize the ER diagram and review the API design.
- **Luis:** Manage repository and Project board setup, choose the database, and validate OAuth with PKCE.

Project board:

Sprint 1 milestone: October 10, 2026
```
