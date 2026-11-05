# Build and Deployment Diagram — TRS Episode 01

The flow is grouped into three compact stages for easier screen-share viewing.

```mermaid
flowchart TB
    subgraph buildStage["1 · Build on control node"]
        direction LR
        source["Spring Boot source"] --> compile["Build + test<br/>Maven / Gradle"] --> image["Versioned container image"] --> archive["Image archive<br/>.tar"]
    end

    subgraph deployStage["2 · Deploy with Ansible"]
        direction LR
        copy["Copy archive<br/>over SSH"] --> load["Load image"] --> reconcile["Create / reconcile<br/>container"] --> health["Wait for health check"]
    end

    subgraph targetStage["3 · Target host · rootless Podman"]
        direction LR
        store[("Local image store")] --> app["Spring Boot API<br/>container"]
    end

    archive --> copy
    load --> store
    reconcile --> app
    health -. "GET /actuator/health" .-> app
    recovery["Idempotent re-run: changed=0<br/>Recovery: remove container, then re-run"] -.-> reconcile

    classDef sourceNode fill:#eef2ff,stroke:#4f46e5,color:#111827,stroke-width:1.5px
    classDef buildNode fill:#f8fafc,stroke:#64748b,color:#111827,stroke-width:1.5px
    classDef artifactNode fill:#fff7ed,stroke:#c2410c,color:#111827,stroke-width:1.5px
    classDef deployNode fill:#ecfeff,stroke:#0e7490,color:#111827,stroke-width:1.5px
    classDef runtimeNode fill:#ecfdf5,stroke:#15803d,color:#111827,stroke-width:1.5px
    classDef recoveryNode fill:#fefce8,stroke:#a16207,color:#111827,stroke-dasharray:4 3

    class source sourceNode
    class compile buildNode
    class image,archive artifactNode
    class copy,load,reconcile,health deployNode
    class store,app runtimeNode
    class recovery recoveryNode
```

See [architecture.md](architecture.md) for the workflow and security principles.
