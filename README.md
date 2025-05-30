# kamp

personal site / portfolio built with Ktor and [kapsule](https://github.com/sakethpathike/kapsule).

### Request Flow
```mermaid
sequenceDiagram
    Client->>kamp: GET /
    activate kamp
    kamp->>kapsule: Build HTML string
    kapsule-->>kamp: Raw HTML string
    kamp->>Client: Full HTML document
    deactivate kamp
    Note over kamp: Generates full HTML
```

### kamp?
*kamp* as in _camp_. Maybe not Clemens Point or Horseshoe Overlook, but on the internet.
