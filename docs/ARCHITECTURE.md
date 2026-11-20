# FlowGuard AI – System Architecture Specification

## 1. Executive Overview

**FlowGuard AI** is a production-grade enterprise Observability & Incident Management SaaS platform engineered to monitor distributed REST APIs, trace multi-hop transactions, ingest centralized logs, automatically track microservice topology, and orchestrate intelligent root-cause analysis (RCA) and conversational diagnostic assistance using AI.

```
       +-------------------------------------------------------------+
       |           FlowGuard AI Modern Enterprise React UI           |
       |      (Dark Navy & Slate / Electric Blue Accent / Vite)      |
       +------------------------------+------------------------------+
                                      | HTTPS / REST / SSE
                                      v
       +-------------------------------------------------------------+
       |               FlowGuard API Gateway / Security              |
       |       (JWT Auth, Rate Limiter, SSRF Guard, RBAC, Audit)     |
       +------------------------------+------------------------------+
                                      |
       +------------------------------v------------------------------+
       |             Modular Spring Boot 3 Core Backend              |
       |  +--------------------+ +--------------------+ +----------+ |
       |  | API Monitor Engine | | Transaction Engine | | Log Svc  | |
       |  +--------------------+ +--------------------+ +----------+ |
       |  | Incident Manager   | | Service Registry   | | AI Eng   | |
       |  +--------------------+ +--------------------+ +----------+ |
       |  | Alert Evaluator    | | Analytics Engine   | | Teams    | |
       |  +--------------------+ +--------------------+ +----------+ |
       +-----------+----------------------+-------------------+------+
                   |                      |                   |
                   v                      v                   v
          +-----------------+    +-----------------+    +------------+
          | PostgreSQL 16   |    | Redis 7 Cache   |    | Apache     |
          | Primary Storage |    | Metrics & Rates |    | Kafka      |
          | Normalized DDL  |    | Session State   |    | Event Bus  |
          +-----------------+    +-----------------+    +------------+
                                                              |
                                                              v
                                                    +------------------+
                                                    | OpenAI API (LLM) |
                                                    | Guarded & Quoted |
                                                    +------------------+
```

---

## 2. Architectural Principles & Non-Functional Requirements

1. **Separation of Concerns & Modularity**: The backend follows a Clean Architecture design (Controller -> Service -> Repository -> Entity/DTO/Mapper) partitioned into strict business domains. Every domain is isolated with clean DTO interfaces, facilitating future extraction into microservices.
2. **Security & Zero Trust**:
   - Strict Role-Based Access Control (RBAC): `SUPER_ADMIN`, `ADMIN`, `ENGINEER`, `VIEWER`.
   - SSRF Protection: API monitor requests pass through an SSRF validator blocking loopback (`127.0.0.1`, `localhost`), link-local (`169.254.0.0/16`), RFC 1918 private subnets (unless explicitly white-labeled), and cloud metadata endpoints (`http://169.254.169.254`).
   - AI Prompt Injection Defense: Application logs, exception traces, and user input fed to AI models are strictly sanitized, escaped, and framed with delimiters and system directives to prevent adversarial prompt injection.
   - Credentials & Secrets: Passed strictly via environment variables; never hardcoded or leaked into client-facing responses.
3. **High Throughput & Asynchronous Event-Driven Architecture**:
   - Heavy telemetry workloads (log ingestion, transaction steps, monitoring metric rollups) are processed asynchronously via Apache Kafka.
   - Resilient producer-consumer pipelines with dead-letter topics (DLQ) and retry mechanisms.
   - Redis caching for P95/P99 latency calculations, dashboard KPIs, and token blacklisting/rate-limiting.
4. **Resilient Data Design**:
   - Normalized relational schema in PostgreSQL using UUID v4 primary keys.
   - High-performance indexing on high-cardinality fields (`trace_id`, `correlation_id`, `service_id`, `status`, `timestamp`).
   - Database schema evolutions managed via repeatable Flyway migrations.

---

## 3. Core Business Domains & Backend Package Layout

| Domain / Package | Responsibilities |
| :--- | :--- |
| `auth` | User authentication, JWT issuance & token refresh, password hashing with BCrypt, RBAC filters |
| `user` | User account lifecycle, profile settings, credentials management |
| `team` | Multi-tenant team management, team member assignments, role mappings |
| `apimonitor` | API monitor CRUD, health check scheduling, SSRF-safe HTTP execution, uptime/latency history |
| `transaction` | Distributed transaction tracking, correlation ID propagation, execution step graphs, failure tracing |
| `serviceregistry`| Microservice registration, dependency graph topology, heartbeat monitoring, health states |
| `logmanagement` | Centralized log ingestion, full-text filtering, severity index, stack trace viewer |
| `incident` | Incident state machine (`DETECTED` -> `ACKNOWLEDGED` -> `INVESTIGATING` -> `RESOLVED` -> `CLOSED`), timeline, events, comments |
| `alert` | Threshold rules (error rate, latency, health), evaluation engine, alert history, alert acknowledgement |
| `ai` | OpenAI API client, Root Cause Analysis synthesis, prompt sanitizer, conversational monitoring assistant |
| `analytics` | Aggregated telemetry, P95/P99 response times, availability rollups, CSV reporting exports |
| `notification` | In-app notification center, user preferences, notification dispatch abstraction (Slack/Email ready) |
| `audit` | Comprehensive audit trail for sensitive administrative, configuration, and security operations |
| `common` | Global exception handler, unified API response wrapper, DTO base classes, security utilities |
| `event` | Kafka producers, consumers, topic definitions, deserializers, DLQ dead-letter handling |

---

## 4. Frontend Architecture & Design System

### 4.1 Theme & Aesthetics
- **Theme**: Dark Navy & Slate base (`#0B0F19`, `#111827`, `#1E293B`) with Slate borders (`#334155`).
- **Accent**: Electric Blue (`#3B82F6` / `#2563EB` / `#60A5FA`).
- **Status Indicators**:
  - Healthy / Success: Emerald (`#10B981`)
  - Warning / Degraded / Medium: Amber (`#F59E0B`)
  - Error / Failed / Critical: Rose/Crimson (`#EF4444`)
  - Info / Pending: Electric Sky (`#0EA5E9`)
- **Typography**: Inter / sans-serif with monospace accents for IDs, timestamps, and JSON stack traces.

### 4.2 Application Route Hierarchy
- `/login` & `/register` & `/forgot-password` & `/reset-password`
- `/dashboard` (Executive KPI overview, live traffic, error rates, active incidents)
- `/api-monitors` & `/api-monitors/:id` (Monitor registration, test runs, latency percentiles)
- `/transactions` & `/transactions/:id` (Transaction explorer, distributed step waterfall)
- `/services` & `/services/:id` (Microservice registry, dependency topology graph)
- `/logs` (Log explorer, full-text search, level filters, stack trace modal)
- `/incidents` & `/incidents/:id` (Incident command center, timeline, comments, AI RCA panel)
- `/alerts` (Alert rule builder, active alerts, notification history)
- `/analytics` (Performance reports, availability metrics, CSV export)
- `/ai-assistant` (Conversational observability copilot)
- `/settings` & `/profile` (Teams, roles, credentials, audit logs)

