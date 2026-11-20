-- ==============================================================================
-- FLOWGUARD AI - PRODUCTION POSTGRESQL SCHEMA INITIALIZATION (V1)
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------------------------
-- 1. Identity & Access Management (Users, Roles, User Roles, Teams, Team Members)
-- ------------------------------------------------------------------------------
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE team_members (
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id)
);

-- ------------------------------------------------------------------------------
-- 2. Services & Microservice Topology
-- ------------------------------------------------------------------------------
CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    service_name VARCHAR(100) NOT NULL UNIQUE,
    service_identifier VARCHAR(100) NOT NULL UNIQUE,
    environment VARCHAR(50) NOT NULL DEFAULT 'PRODUCTION',
    base_url VARCHAR(500),
    version VARCHAR(50),
    health_status VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',
    last_heartbeat TIMESTAMPTZ,
    average_latency_ms DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_dependencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    target_service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    dependency_type VARCHAR(50) NOT NULL DEFAULT 'REST',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_dep UNIQUE (source_service_id, target_service_id)
);

-- ------------------------------------------------------------------------------
-- 3. API Monitoring
-- ------------------------------------------------------------------------------
CREATE TABLE api_monitors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    endpoint_path VARCHAR(500) NOT NULL,
    http_method VARCHAR(10) NOT NULL DEFAULT 'GET',
    environment VARCHAR(50) NOT NULL DEFAULT 'PRODUCTION',
    request_headers JSONB,
    request_body TEXT,
    auth_config JSONB,
    monitoring_interval_seconds INT NOT NULL DEFAULT 60,
    timeout_ms INT NOT NULL DEFAULT 5000,
    expected_http_status INT NOT NULL DEFAULT 200,
    alert_threshold_ms INT NOT NULL DEFAULT 2000,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    current_status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    last_checked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE monitoring_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    monitor_id UUID NOT NULL REFERENCES api_monitors(id) ON DELETE CASCADE,
    status_code INT NOT NULL,
    response_time_ms INT NOT NULL,
    is_successful BOOLEAN NOT NULL,
    error_message TEXT,
    response_headers JSONB,
    response_body_sample TEXT,
    checked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 4. Transaction Intelligence
-- ------------------------------------------------------------------------------
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    correlation_id VARCHAR(100) NOT NULL,
    request_id VARCHAR(100),
    transaction_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMPTZ,
    total_duration_ms BIGINT,
    root_service_id UUID REFERENCES services(id) ON DELETE SET NULL,
    failure_reason TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    service_id UUID REFERENCES services(id) ON DELETE SET NULL,
    step_name VARCHAR(150) NOT NULL,
    step_order INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    duration_ms INT NOT NULL DEFAULT 0,
    error_message TEXT,
    payload_snapshot TEXT,
    executed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 5. Centralized Log Management
-- ------------------------------------------------------------------------------
CREATE TABLE application_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID REFERENCES services(id) ON DELETE SET NULL,
    service_name VARCHAR(100) NOT NULL,
    environment VARCHAR(50) NOT NULL DEFAULT 'PRODUCTION',
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message TEXT NOT NULL,
    trace_id VARCHAR(100),
    correlation_id VARCHAR(100),
    exception_details TEXT,
    logger_name VARCHAR(255),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

-- ------------------------------------------------------------------------------
-- 6. Incident Management & AI Root Cause Analysis
-- ------------------------------------------------------------------------------
CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'DETECTED',
    affected_service_id UUID REFERENCES services(id) ON DELETE SET NULL,
    related_api_id UUID REFERENCES api_monitors(id) ON DELETE SET NULL,
    assigned_engineer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    resolution_notes TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE incident_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE incident_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_description TEXT NOT NULL,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    summary TEXT NOT NULL,
    potential_root_causes JSONB NOT NULL,
    affected_services JSONB NOT NULL,
    relevant_error_patterns JSONB NOT NULL,
    recommended_investigation_steps JSONB NOT NULL,
    suggested_remediation_actions JSONB NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    supporting_evidence TEXT,
    model_used VARCHAR(100) NOT NULL DEFAULT 'gpt-4o',
    disclaimer TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 7. Alert Management & Notifications
-- ------------------------------------------------------------------------------
CREATE TABLE alert_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID,
    metric_type VARCHAR(50) NOT NULL,
    comparison_operator VARCHAR(10) NOT NULL,
    threshold_value DOUBLE PRECISION NOT NULL,
    evaluation_window_minutes INT NOT NULL DEFAULT 5,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alert_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_rule_id UUID NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    triggered_value DOUBLE PRECISION NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    is_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by UUID REFERENCES users(id) ON DELETE SET NULL,
    acknowledged_at TIMESTAMPTZ,
    triggered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    link_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 8. Audit Trail
-- ------------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    user_email VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    details JSONB,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 9. Performance Indexes
-- ------------------------------------------------------------------------------
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_monitors_team ON api_monitors(team_id);
CREATE INDEX idx_monitors_status ON api_monitors(current_status);
CREATE INDEX idx_monitoring_results_monitor_time ON monitoring_results(monitor_id, checked_at DESC);
CREATE INDEX idx_services_identifier ON services(service_identifier);
CREATE INDEX idx_transactions_txid ON transactions(transaction_id);
CREATE INDEX idx_transactions_correlation ON transactions(correlation_id);
CREATE INDEX idx_transactions_status_time ON transactions(transaction_status, start_time DESC);
CREATE INDEX idx_tx_steps_tx_order ON transaction_steps(transaction_id, step_order ASC);
CREATE INDEX idx_logs_timestamp ON application_logs(timestamp DESC);
CREATE INDEX idx_logs_service_level ON application_logs(service_name, log_level, timestamp DESC);
CREATE INDEX idx_logs_correlation ON application_logs(correlation_id);
CREATE INDEX idx_logs_trace ON application_logs(trace_id);
CREATE INDEX idx_incidents_status_severity ON incidents(status, severity);
CREATE INDEX idx_incidents_number ON incidents(incident_number);
CREATE INDEX idx_incidents_service ON incidents(affected_service_id);
CREATE INDEX idx_alert_history_rule_time ON alert_history(alert_rule_id, triggered_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);

-- ------------------------------------------------------------------------------
-- 10. Seed Roles
-- ------------------------------------------------------------------------------
INSERT INTO roles (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Platform Super Administrator with complete administrative rights'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Team Administrator with monitor, service and alert management rights'),
    ('00000000-0000-0000-0000-000000000003', 'ENGINEER', 'Software Engineer with incident response, diagnostic and view rights'),
    ('00000000-0000-0000-0000-000000000004', 'VIEWER', 'Read-only viewer with dashboard and analytics access');

