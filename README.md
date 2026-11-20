# FlowGuard AI – Intelligent API Monitoring, Transaction Intelligence & Incident Management Platform

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.5-blue.svg)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.4-38B2AC.svg)](https://tailwindcss.com/)
[![Ollama](https://img.shields.io/badge/AI%20Engine-Ollama%20(Open--Source)-purple.svg)](https://ollama.com/)
[![License](https://img.shields.io/badge/License-Enterprise%20SaaS-electric.svg)]()

FlowGuard AI is a production-grade enterprise observability and transaction intelligence SaaS platform. Engineered for distributed microservice architectures, it provides end-to-end REST API health probing with kernel-level SSRF defense, multi-service distributed transaction waterfall tracing, high-throughput centralized log querying, incident command center triage, and **free, local, open-source AI integration powered by Ollama** (default: `qwen2.5:7b` / `qwen3:8b`) with zero mandatory cloud subscriptions.

---

## 1. Zero Hardcoded Business Data Architecture

Every business metric, chart point, incident status, and transaction displayed across the platform is calculated dynamically from PostgreSQL via Spring Boot REST APIs:

```
React 18 Frontend  ──(Axios + TanStack Query)──►  Spring Boot 3 REST API  ──►  Service Layer  ──►  Spring Data JPA  ──►  PostgreSQL 16
                                                                             └──►  Ollama AI Provider  ──►  Local LLM (qwen2.5:7b)
```

- **Dynamic KPI Aggregation:** Requests, latencies, P95 percentiles, availability percentages, and incident counts calculated dynamically from `monitoring_results`, `transactions`, and `incidents` tables.
- **Dynamic Telemetry Charts:** Area, bar, and line time-series generated dynamically from database timestamps. Empty states are rendered if no records exist.
- **Zero Hallucination / Mock AI Data:** When Ollama is offline, the platform provides clean, transparent telemetry snapshots without fake or hallucinated predictions.

---

## 2. Core Platform Modules

### 🔐 Module 1: Enterprise Authentication & RBAC
- **Stateless JWT Security:** Access tokens and rolling refresh tokens via JJWT 0.12.6.
- **Case-Insensitive Login & Trimming:** Clean, tolerant authentication across all clients.
- **Hierarchical Roles:**
  - `SUPER_ADMIN`: Complete global administrative access, team management, and audit logs.
  - `ADMIN`: Alert rules, thresholds, and monitor lifecycle configuration.
  - `ENGINEER`: Incident declaration, investigation notes, synthetic probing, and trace diagnostics.
  - `VIEWER`: Read-only telemetry dashboards, charts, and audit histories.

### 📊 Module 2: Executive Telemetry Dashboard
- **8 Dynamic KPI Cards:** Total Requests, Successful Requests, Failed Requests, Average Latency, P95 Response Time, Platform Availability %, Active Incidents, and Transaction Success Rate.
- **Interactive Recharts Visualizations:** API traffic velocity, error rates, and P95 latency percentiles.
- **Dynamic Summaries:** Top failing APIs, slowest APIs, microservice health topology, and recent distributed transactions.

### ⚡ Module 3: Proactive API Monitoring & SSRF Protection
- **Configurable Probes:** Supports GET, POST, PUT, DELETE, PATCH, and HEAD with custom headers, bodies, timeouts, intervals, and expected HTTP status codes.
- **SSRF Kernel Defense:** `SsrfProtectionValidator` blocks loopback (`127.0.0.1`, `localhost`), link-local (`169.254.0.0/16`), AWS/GCP cloud metadata (`169.254.169.254`), and unauthorized private RFC 1918 subnets.
- **Automated & On-Demand Health Probes:** Trigger immediate synthetic probes with real-time feedback or schedule recurring health checks.

### 🌐 Module 4: Distributed Transaction Intelligence & Waterfall Tracing
- **Multi-Service Tracing:** Multi-hop business workflows across gateways, order services, payment providers, inventory systems, and notification dispatchers.
- **Execution Waterfall:** Millisecond duration for each service hop, status tags (`SUCCESS`, `FAILED`, `SKIPPED`), and payload snapshots.
- **Correlation ID Tracking:** Query across human-readable Transaction IDs and distributed Correlation IDs.

### 📜 Module 5: Centralized Application Log Management
- **High-Throughput Ingestion:** Single and batch ingestion endpoints for microservice log shippers.
- **Multi-Dimension Search:** Filter across log levels (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`), microservice names, and correlation IDs with live auto-refresh streaming.
- **Stack Trace Inspector:** Modal diagnostics with one-click copy and JSON metadata viewers.

### 🚨 Module 6: Incident Management Command Center
- **Incident State Machine:** Step through formal triage phases: `DETECTED` ➔ `ACKNOWLEDGED` ➔ `INVESTIGATING` ➔ `RESOLVED` ➔ `CLOSED`.
- **Severity Tagging:** `CRITICAL` (pulsing indicator), `HIGH`, `MEDIUM`, and `LOW`.
- **Collaborative Investigation Thread:** Real-time engineering comments and audit trail timelines.

### 🔔 Module 7: Threshold Alert Rules & Notification Engine
- **Proactive Boundary Rules:** Configure metric thresholds on Response Time (ms), Error Rate (%), Availability Uptime (%), and Consecutive Failure counts.
- **Triggered Event History:** Live acknowledgement tracking for unacknowledged alerts.

### 🤖 Module 8: Local Open-Source AI (Ollama Integration)
- **Zero Paid Subscription:** Operates with a local, free, open-weight LLM using Ollama (default: `qwen2.5:7b`, supports `qwen3:8b`, `llama3.1:8b`).
- **Incident Root Cause Analysis (RCA):** Synthesizes live incident details, related error logs, failed transactions, and probe latencies into structured RCA reports.
- **Natural Language Monitoring Assistant:** Interactive copilot answering questions like *"Why did the payment service fail?"* and *"Show me slowest endpoints"* based strictly on database telemetry.
- **Safety Guardrails:** Mandatory advisory disclaimer, PII / Bearer token redaction, and offline detection.

---

## 3. Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 18, TypeScript 5.5, Vite 6, Tailwind CSS 3.4, React Router 6, TanStack React Query, Axios, Recharts, Lucide React |
| **Backend** | Java 21 LTS, Spring Boot 3.4.1, Spring Security, Spring Data JPA, Hibernate 6, Spring Actuator, Springdoc OpenAPI 2.7, JJWT 0.12.6, Flyway |
| **Database & Cache** | PostgreSQL 16 (Primary relational schema), Redis 7 (Metric caching & rate-limiting), H2 (In-memory local mode) |
| **Messaging** | Apache Kafka 3.8 (Event streaming & telemetry fan-out) |
| **AI Integration** | Ollama REST API (Local open-weight LLM: `qwen2.5:7b` / `qwen3:8b`), optional OpenAI fallback |
| **DevOps** | Docker, Multi-stage Dockerfile, Docker Compose, Nginx (Alpine) |

---

## 4. Pre-Seeded Demo Credentials

The platform includes pre-seeded enterprise accounts with pre-configured privileges. The login page provides 1-click credential fill buttons:

| Email | Password | Role | Permissions |
|---|---|---|---|
| `admin@flowguard.ai` | `Admin@123456` | **SUPER_ADMIN** | Full administrative and organizational control |
| `engineer@flowguard.ai` | `Engineer@123456` | **ENGINEER** | Probes, incident triage, and transaction tracing |
| `viewer@flowguard.ai` | `Viewer@123456` | **VIEWER** | Read-only dashboards and telemetry analytics |

---

## 5. Swagger / OpenAPI Documentation & Authorization

Swagger UI is publicly accessible without login at:
- **URL:** `http://localhost:8080/swagger-ui/index.html` (or `http://localhost:8080/swagger-ui.html`)
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Authorizing Protected Endpoints in Swagger:
1. Open `http://localhost:8080/swagger-ui/index.html`.
2. Expand `POST /api/v1/auth/login` and submit demo credentials (`admin@flowguard.ai` / `Admin@123456`).
3. Copy the returned `accessToken`.
4. Click the green **Authorize** button at the top right of the Swagger UI.
5. Paste the token into the `BearerAuth` input field and click **Authorize**.
6. All protected business endpoints (`/api/v1/monitors`, `/api/v1/incidents`, `/api/v1/transactions`, `/api/v1/ai/...`) are now authorized for interactive testing.

---

## 6. Local AI Setup with Ollama (Free & Open-Source)

FlowGuard AI integrates with a local Ollama instance for zero-cost, private AI capabilities:

1. **Install Ollama:**
   - Download and install from [ollama.com](https://ollama.com/download) (available for Windows, macOS, Linux).

2. **Pull the Default Model:**
   ```bash
   ollama pull qwen2.5:7b
   ```
   *(Or `ollama pull qwen3:8b` / `ollama pull llama3.1:8b`)*

3. **Start the Ollama Server:**
   ```bash
   ollama serve
   ```
   *Ollama listens at `http://localhost:11434`.*

4. **Configuration (Environment Variables):**
   ```bash
   AI_PROVIDER=ollama
   OLLAMA_BASE_URL=http://localhost:11434
   OLLAMA_MODEL=qwen2.5:7b
   ```
   *(If Ollama is not running, FlowGuard AI automatically detects the offline state and presents clean telemetry snapshots without fake data).*

---

## 7. Quickstart Guide

### Option A: Local Development (Fastest, Zero External Dependencies)

FlowGuard AI defaults to the `local` Spring Boot profile, which boots an in-memory PostgreSQL-compatible database and auto-seeds all sample telemetry:

1. **Start the Backend:**
   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
   ```
   *The backend starts at `http://localhost:8080`.*

2. **Start the Frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *The UI starts at `http://localhost:5173`.*

---

### Option B: Full Production Docker Compose

Run the complete multi-tier enterprise stack (PostgreSQL 16, Redis 7, Kafka 3.8 KRaft, Spring Boot Backend, and Nginx-served React Frontend):

```bash
docker compose up --build
```

- **Frontend Application:** `http://localhost:3000`
- **Backend API & Actuator:** `http://localhost:8080`
- **Swagger Documentation:** `http://localhost:8080/swagger-ui/index.html`
- **PostgreSQL Database:** `localhost:5432` (`flowguard` / `flowguard_secret`)
- **Redis Cache:** `localhost:6379`
- **Apache Kafka:** `localhost:9092`

---

## 8. Verification & Test Suite

Run the complete JUnit 5 and Mockito test suite:

```bash
cd backend
.\mvnw.cmd test
```
*Result: 18 tests executed, 0 failures, 0 errors, 0 skipped.*

Build the production frontend distribution:

```bash
cd frontend
npm run build
```
*Result: 0 TypeScript errors, production bundles emitted in `dist/`.*
