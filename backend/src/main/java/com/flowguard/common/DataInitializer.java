package com.flowguard.common;

import com.flowguard.alert.AlertHistory;
import com.flowguard.alert.AlertHistoryRepository;
import com.flowguard.alert.AlertRule;
import com.flowguard.alert.AlertRuleRepository;
import com.flowguard.alert.ComparisonOperator;
import com.flowguard.alert.MetricType;
import com.flowguard.alert.TargetType;
import com.flowguard.serviceregistry.ServiceEntity;
import com.flowguard.serviceregistry.ServiceRepository;
import com.flowguard.apimonitor.ApiMonitor;
import com.flowguard.apimonitor.ApiMonitorRepository;
import com.flowguard.apimonitor.EnvironmentType;
import com.flowguard.apimonitor.HttpMethod;
import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.apimonitor.MonitoringResult;
import com.flowguard.apimonitor.MonitoringResultRepository;
import com.flowguard.incident.Incident;
import com.flowguard.incident.IncidentComment;
import com.flowguard.incident.IncidentCommentRepository;
import com.flowguard.incident.IncidentEvent;
import com.flowguard.incident.IncidentEventRepository;
import com.flowguard.incident.IncidentRepository;
import com.flowguard.incident.IncidentSeverity;
import com.flowguard.incident.IncidentStatus;
import com.flowguard.logmanagement.ApplicationLog;
import com.flowguard.logmanagement.ApplicationLogRepository;
import com.flowguard.logmanagement.LogLevel;
import com.flowguard.team.Team;
import com.flowguard.team.TeamMember;
import com.flowguard.team.TeamMemberId;
import com.flowguard.team.TeamMemberRepository;
import com.flowguard.team.TeamRepository;
import com.flowguard.team.TeamRole;
import com.flowguard.transaction.StepStatus;
import com.flowguard.transaction.Transaction;
import com.flowguard.transaction.TransactionRepository;
import com.flowguard.transaction.TransactionStatus;
import com.flowguard.transaction.TransactionStep;
import com.flowguard.transaction.TransactionStepRepository;
import com.flowguard.user.Role;
import com.flowguard.user.RoleRepository;
import com.flowguard.user.RoleType;
import com.flowguard.user.User;
import com.flowguard.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ApiMonitorRepository monitorRepository;
    private final MonitoringResultRepository resultRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionStepRepository stepRepository;
    private final ApplicationLogRepository logRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentEventRepository eventRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking FlowGuard AI initial seed data...");

        Role superAdminRole = getOrCreateRole(RoleType.SUPER_ADMIN, "Super Administrator with complete privileges");
        Role adminRole = getOrCreateRole(RoleType.ADMIN, "Team Administrator with management privileges");
        Role engineerRole = getOrCreateRole(RoleType.ENGINEER, "Software Engineer with observability and diagnostic rights");
        Role viewerRole = getOrCreateRole(RoleType.VIEWER, "Read-only viewer with dashboard and analytics access");

        // Seed Super Admin user
        User adminUser = userRepository.findByEmail("admin@flowguard.ai").orElseGet(() -> {
            log.info("Seeding Super Admin user: admin@flowguard.ai");
            User user = User.builder()
                    .email("admin@flowguard.ai")
                    .passwordHash(passwordEncoder.encode("Admin@123456"))
                    .firstName("Alex")
                    .lastName("Vance")
                    .phone("+1-555-0100")
                    .isActive(true)
                    .emailVerified(true)
                    .roles(Set.of(superAdminRole, adminRole))
                    .build();
            return userRepository.save(user);
        });

        // Seed Engineer user
        User engineerUser = userRepository.findByEmail("engineer@flowguard.ai").orElseGet(() -> {
            log.info("Seeding Engineer user: engineer@flowguard.ai");
            User user = User.builder()
                    .email("engineer@flowguard.ai")
                    .passwordHash(passwordEncoder.encode("Engineer@123456"))
                    .firstName("Sarah")
                    .lastName("Chen")
                    .phone("+1-555-0101")
                    .isActive(true)
                    .emailVerified(true)
                    .roles(Set.of(engineerRole))
                    .build();
            return userRepository.save(user);
        });

        // Seed Viewer user
        userRepository.findByEmail("viewer@flowguard.ai").orElseGet(() -> {
            log.info("Seeding Viewer user: viewer@flowguard.ai");
            User user = User.builder()
                    .email("viewer@flowguard.ai")
                    .passwordHash(passwordEncoder.encode("Viewer@123456"))
                    .firstName("Morgan")
                    .lastName("Reed")
                    .phone("+1-555-0102")
                    .isActive(true)
                    .emailVerified(true)
                    .roles(Set.of(viewerRole))
                    .build();
            return userRepository.save(user);
        });

        // Seed Default Team Workspace
        Team team = teamRepository.findAll().stream().findFirst().orElseGet(() -> {
            log.info("Seeding default team workspace: Core Platform");
            Team newTeam = Team.builder()
                    .name("Core Platform Engineering")
                    .slug("core-platform")
                    .description("Primary observability workspace for core microservices and gateway APIs")
                    .build();
            Team savedTeam = teamRepository.save(newTeam);

            teamMemberRepository.save(TeamMember.builder()
                    .id(new TeamMemberId(savedTeam.getId(), adminUser.getId()))
                    .team(savedTeam)
                    .user(adminUser)
                    .role(TeamRole.OWNER)
                    .joinedAt(Instant.now())
                    .build());

            teamMemberRepository.save(TeamMember.builder()
                    .id(new TeamMemberId(savedTeam.getId(), engineerUser.getId()))
                    .team(savedTeam)
                    .user(engineerUser)
                    .role(TeamRole.MEMBER)
                    .joinedAt(Instant.now())
                    .build());
            return savedTeam;
        });

        // Seed Monitored APIs
        seedMonitoredApis(team.getId());

        // Seed Services Topology
        seedServices(team.getId());

        // Seed Distributed Transactions
        seedTransactions();

        // Seed Centralized Logs
        seedApplicationLogs();

        // Seed Incidents
        seedIncidents(engineerUser);

        // Seed Alert Rules & Alert History
        seedAlerts(engineerUser);

        log.info("FlowGuard AI seed data check completed successfully.");
    }

    private void seedMonitoredApis(UUID teamId) {
        if (monitorRepository.count() > 0) {
            return;
        }

        log.info("Seeding 8 enterprise microservice API monitors...");

        List<ApiMonitor> monitors = List.of(
                ApiMonitor.builder()
                        .name("Payment Gateway API")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.POST)
                        .environment(EnvironmentType.PRODUCTION)
                        .requestHeaders("{\"Authorization\": \"Bearer live_sec_mock_token\", \"Content-Type\": \"application/json\"}")
                        .requestBody("{\"currency\": \"USD\", \"amount\": 9999}")
                        .monitoringIntervalSeconds(30)
                        .timeoutMs(4000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(1500)
                        .isActive(true)
                        .currentStatus(MonitorStatus.HEALTHY)
                        .lastCheckedAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("User Authentication Service")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.POST)
                        .environment(EnvironmentType.PRODUCTION)
                        .requestHeaders("{\"Content-Type\": \"application/json\"}")
                        .monitoringIntervalSeconds(60)
                        .timeoutMs(3000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(800)
                        .isActive(true)
                        .currentStatus(MonitorStatus.HEALTHY)
                        .lastCheckedAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("Order Fulfillment API")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.POST)
                        .environment(EnvironmentType.PRODUCTION)
                        .requestHeaders("{\"Content-Type\": \"application/json\"}")
                        .monitoringIntervalSeconds(45)
                        .timeoutMs(5000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(2000)
                        .isActive(true)
                        .currentStatus(MonitorStatus.HEALTHY)
                        .lastCheckedAt(Instant.now().minus(3, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("Inventory Reservation Service")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.PUT)
                        .environment(EnvironmentType.PRODUCTION)
                        .requestHeaders("{\"Content-Type\": \"application/json\"}")
                        .monitoringIntervalSeconds(60)
                        .timeoutMs(4000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(1200)
                        .isActive(true)
                        .currentStatus(MonitorStatus.DEGRADED)
                        .lastCheckedAt(Instant.now().minus(4, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("Notification Dispatcher")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.POST)
                        .environment(EnvironmentType.STAGING)
                        .requestHeaders("{\"Content-Type\": \"application/json\"}")
                        .monitoringIntervalSeconds(120)
                        .timeoutMs(5000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(2500)
                        .isActive(true)
                        .currentStatus(MonitorStatus.HEALTHY)
                        .lastCheckedAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("Customer Profile API")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.GET)
                        .environment(EnvironmentType.PRODUCTION)
                        .requestHeaders("{\"Accept\": \"application/json\"}")
                        .monitoringIntervalSeconds(60)
                        .timeoutMs(3000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(1000)
                        .isActive(true)
                        .currentStatus(MonitorStatus.HEALTHY)
                        .lastCheckedAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("Fraud Risk Scoring Engine")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/500")
                        .httpMethod(HttpMethod.POST)
                        .environment(EnvironmentType.PRODUCTION)
                        .requestHeaders("{\"Content-Type\": \"application/json\"}")
                        .monitoringIntervalSeconds(30)
                        .timeoutMs(5000)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(1500)
                        .isActive(true)
                        .currentStatus(MonitorStatus.DOWN)
                        .lastCheckedAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build(),

                ApiMonitor.builder()
                        .name("Catalog Search Index")
                        .baseUrl("https://httpbin.org")
                        .endpointPath("/status/200")
                        .httpMethod(HttpMethod.GET)
                        .environment(EnvironmentType.DEVELOPMENT)
                        .requestHeaders("{\"Accept\": \"application/json\"}")
                        .monitoringIntervalSeconds(30)
                        .timeoutMs(2500)
                        .expectedHttpStatus(200)
                        .alertThresholdMs(600)
                        .isActive(true)
                        .currentStatus(MonitorStatus.HEALTHY)
                        .lastCheckedAt(Instant.now().minus(2, ChronoUnit.MINUTES))
                        .teamId(teamId)
                        .build()
        );

        List<ApiMonitor> savedMonitors = monitorRepository.saveAll(monitors);

        // Seed historical probe runs for each monitor
        Random random = new Random(42);
        for (ApiMonitor m : savedMonitors) {
            List<MonitoringResult> history = new ArrayList<>();
            int totalProbes = 15;
            for (int i = totalProbes; i >= 1; i--) {
                Instant probeTime = Instant.now().minus(i * (long) m.getMonitoringIntervalSeconds(), ChronoUnit.SECONDS);
                boolean isDown = (m.getCurrentStatus() == MonitorStatus.DOWN) || (m.getCurrentStatus() == MonitorStatus.DEGRADED && i % 3 == 0);
                int statusCode = isDown ? 500 : 200;
                int latency = isDown ? 2100 + random.nextInt(800) : 120 + random.nextInt(350);

                history.add(MonitoringResult.builder()
                        .monitorId(m.getId())
                        .statusCode(statusCode)
                        .responseTimeMs(latency)
                        .isSuccessful(!isDown)
                        .errorMessage(isDown ? "HTTP 500: Internal Server Error returned by downstream service" : null)
                        .responseHeaders("{\"server\": \"envoy\", \"content-type\": \"application/json\", \"x-request-id\": \"" + UUID.randomUUID() + "\"}")
                        .responseBodySample(isDown ? "{\"error\": \"Internal server error\", \"timestamp\": \"" + probeTime + "\"}" : "{\"status\": \"OK\"}")
                        .checkedAt(probeTime)
                        .build());
            }
            resultRepository.saveAll(history);
        }
    }

    private void seedTransactions() {
        if (transactionRepository.count() > 0) {
            return;
        }

        log.info("Seeding 50 realistic distributed transactions across microservices...");

        String[] workflows = {
                "E-Commerce Checkout",
                "User Onboarding & KYC",
                "Instant Subscription Billing",
                "Cross-Border Wire Transfer",
                "Inventory Restock Sync",
                "Customer Password Reset",
                "Bulk Invoicing Pipeline",
                "Fraud Quarantine Flow"
        };

        String[] rootServices = {
                "api-gateway",
                "checkout-service",
                "auth-service",
                "billing-service",
                "transfer-orchestrator",
                "warehouse-service"
        };

        Random random = new Random(1337);

        for (int i = 1; i <= 50; i++) {
            String workflow = workflows[random.nextInt(workflows.length)];
            String txId = String.format("TXN-%05d-%s", 1000 + i, workflow.substring(0, 3).toUpperCase());
            String corrId = "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String rootService = rootServices[random.nextInt(rootServices.length)];

            TransactionStatus status;
            if (i % 8 == 0) {
                status = TransactionStatus.FAILED;
            } else if (i == 49) {
                status = TransactionStatus.PROCESSING;
            } else if (i == 50) {
                status = TransactionStatus.PENDING;
            } else {
                status = TransactionStatus.SUCCESS;
            }

            Instant start = Instant.now().minus(i * 18, ChronoUnit.MINUTES);
            long totalDuration = 0L;
            String failureReason = status == TransactionStatus.FAILED ? "Downstream timeout or business rule rejection" : null;

            Transaction tx = Transaction.builder()
                    .transactionId(txId)
                    .correlationId(corrId)
                    .requestId("REQ-" + UUID.randomUUID().toString().substring(0, 8))
                    .transactionStatus(status)
                    .rootServiceName(rootService)
                    .startTime(start)
                    .metadata("{\"workflow\": \"" + workflow + "\", \"region\": \"us-east-1\", \"retry_count\": " + (status == TransactionStatus.FAILED ? 2 : 0) + "}")
                    .totalDurationMs(0L)
                    .failureReason(failureReason)
                    .build();

            Transaction savedTx = transactionRepository.save(tx);

            // Generate 3 to 5 steps per transaction
            List<TransactionStep> steps = new ArrayList<>();
            int numSteps = 3 + (i % 3);

            String[] stepNames = {
                    "Authorize API Request",
                    "Validate User Session",
                    "Reserve Inventory Allocation",
                    "Authorize Payment Charge",
                    "Dispatch Confirmation Webhook"
            };

            String[] serviceHops = {
                    "api-gateway",
                    "auth-service",
                    "inventory-service",
                    "payment-service",
                    "notification-service"
            };

            for (int s = 0; s < numSteps; s++) {
                int stepDuration = 45 + random.nextInt(320);
                boolean stepFailed = (status == TransactionStatus.FAILED && s == numSteps - 1);
                StepStatus stepStatus = stepFailed ? StepStatus.FAILED : (status == TransactionStatus.PENDING ? StepStatus.SKIPPED : StepStatus.SUCCESS);

                String stepError = stepFailed ? "Connection refused to payment provider gateway (stripe-adapter:443)" : null;

                TransactionStep step = TransactionStep.builder()
                        .transaction(savedTx)
                        .serviceName(serviceHops[s % serviceHops.length])
                        .stepName(stepNames[s % stepNames.length])
                        .stepOrder(s + 1)
                        .status(stepStatus)
                        .durationMs(stepDuration)
                        .errorMessage(stepError)
                        .payloadSnapshot("{\"step_index\": " + (s + 1) + ", \"service\": \"" + serviceHops[s % serviceHops.length] + "\", \"status\": \"" + stepStatus + "\"}")
                        .executedAt(start.plus(totalDuration, ChronoUnit.MILLIS))
                        .build();

                totalDuration += stepDuration;
                steps.add(step);
            }

            stepRepository.saveAll(steps);

            savedTx.setTotalDurationMs(totalDuration);
            savedTx.setEndTime(start.plus(totalDuration, ChronoUnit.MILLIS));
            transactionRepository.save(savedTx);
        }
    }

    private void seedApplicationLogs() {
        if (logRepository.count() > 0) {
            return;
        }

        log.info("Seeding 100 realistic application logs across microservices...");

        String[] services = {"payment-service", "auth-service", "inventory-service", "notification-service", "api-gateway", "fraud-service"};
        String[] errorMessages = {
                "SocketTimeoutException: Downstream bank settlement connector timed out after 5000ms",
                "OptimisticLockingFailureException: Concurrent inventory mutation detected on SKU-99182",
                "JsonParseException: Unexpected character in webhook dispatch payload at offset 124",
                "JwtSignatureException: Signature verification failed for token expired at 1718291000",
                "ConnectionRefusedException: Unable to acquire connection from pool hikari-pool-db",
                "RateLimitExceededException: Client IP 198.51.100.41 exceeded 500 req/min bucket"
        };

        String[] infoMessages = {
                "Processed incoming authorization charge for transaction",
                "User credentials verified successfully and JWT session minted",
                "Inventory reservation locked for batch order fulfillment",
                "Webhook event notification dispatched to customer callback URL",
                "Ingress route matched and proxy forwarded to upstream cluster",
                "Fraud evaluation completed with risk score 0.04 (ACCEPTED)"
        };

        Random random = new Random(2026);
        List<ApplicationLog> logs = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            String service = services[random.nextInt(services.length)];
            LogLevel level;
            String message;
            String exception = null;

            if (i % 6 == 0) {
                level = LogLevel.ERROR;
                message = errorMessages[random.nextInt(errorMessages.length)];
                exception = "java.util.concurrent.ExecutionException: " + message + "\n\tat com.flowguard.service.Connector.execute(Connector.java:84)\n\tat com.flowguard.service.Worker.run(Worker.java:112)";
            } else if (i % 8 == 0) {
                level = LogLevel.WARN;
                message = "High latency detected during downstream call: " + (1200 + random.nextInt(1500)) + "ms";
            } else {
                level = LogLevel.INFO;
                message = infoMessages[random.nextInt(infoMessages.length)] + " [" + UUID.randomUUID().toString().substring(0, 8) + "]";
            }

            Instant time = Instant.now().minus(i * 3, ChronoUnit.MINUTES);
            String corrId = "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String traceId = "TRACE-" + UUID.randomUUID().toString().substring(0, 12).toLowerCase();

            logs.add(ApplicationLog.builder()
                    .serviceName(service)
                    .environment("PRODUCTION")
                    .logLevel(level)
                    .message(message)
                    .traceId(traceId)
                    .correlationId(corrId)
                    .exceptionDetails(exception)
                    .loggerName("com.flowguard." + service.replace("-", ".") + ".ServiceLogger")
                    .timestamp(time)
                    .metadata("{\"host\": \"k8s-pod-" + service + "-" + (i % 4 + 1) + "\", \"dc\": \"us-east-1\"}")
                    .build());
        }

        logRepository.saveAll(logs);
    }

    private void seedIncidents(User assignedUser) {
        if (incidentRepository.count() > 0) {
            return;
        }

        log.info("Seeding 6 realistic engineering incidents and timeline histories...");

        List<Incident> incidents = List.of(
                Incident.builder()
                        .incidentNumber("INC-2026-001")
                        .title("Elevated HTTP 504 Gateway Timeouts in Payment Processing Gateway")
                        .description("Over 4.8% of payment authorization calls are failing with 504 Gateway Timeout. Traces indicate downstream bank connector is exceeding 5000ms latency ceiling.")
                        .severity(IncidentSeverity.CRITICAL)
                        .status(IncidentStatus.INVESTIGATING)
                        .assignedEngineerId(assignedUser.getId())
                        .assignedEngineerName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                        .build(),

                Incident.builder()
                        .incidentNumber("INC-2026-002")
                        .title("Optimistic Locking Failure Spike in Inventory Reservation Service")
                        .description("Concurrent flash-sale orders are causing OptimisticLockingFailureException during SKU inventory stock decrement. Retry queue saturation observed.")
                        .severity(IncidentSeverity.HIGH)
                        .status(IncidentStatus.ACKNOWLEDGED)
                        .assignedEngineerId(assignedUser.getId())
                        .assignedEngineerName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                        .build(),

                Incident.builder()
                        .incidentNumber("INC-2026-003")
                        .title("Third-Party SMS Provider Webhook Authentication Failure")
                        .description("Notification Dispatcher failed 84 webhook delivery attempts due to upstream provider rejecting SSL handshake. Rolled back certificate patch.")
                        .severity(IncidentSeverity.MEDIUM)
                        .status(IncidentStatus.RESOLVED)
                        .resolutionNotes("Rotated client mTLS certificates and flushed dead-letter queue. All pending deliveries completed.")
                        .resolvedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                        .assignedEngineerId(assignedUser.getId())
                        .assignedEngineerName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                        .build(),

                Incident.builder()
                        .incidentNumber("INC-2026-004")
                        .title("Increased P95 Latency on Customer Profile GraphQL Queries")
                        .description("Query response time degraded to 2,400ms following cache invalidation. Added database index on customer_profiles(user_id, status).")
                        .severity(IncidentSeverity.LOW)
                        .status(IncidentStatus.CLOSED)
                        .resolutionNotes("Index applied and warmup query run. P95 latency restored to 68ms.")
                        .resolvedAt(Instant.now().minus(5, ChronoUnit.HOURS))
                        .assignedEngineerId(assignedUser.getId())
                        .assignedEngineerName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                        .build(),

                Incident.builder()
                        .incidentNumber("INC-2026-005")
                        .title("Fraud Risk Scoring Engine Connection Refusal")
                        .description("Fraud scoring cluster pods experiencing OOMKilled crash loops under high ingestion spikes. Gateway falling back to conservative acceptance mode.")
                        .severity(IncidentSeverity.CRITICAL)
                        .status(IncidentStatus.DETECTED)
                        .build(),

                Incident.builder()
                        .incidentNumber("INC-2026-006")
                        .title("Catalog Search Index Cache Eviction Cascading")
                        .description("Redis memory limit reached, triggering key evictions and causing catalog search latency to spike during peak hours.")
                        .severity(IncidentSeverity.MEDIUM)
                        .status(IncidentStatus.RESOLVED)
                        .resolutionNotes("Scaled Redis cache memory to 8GB and enabled volatile-lru eviction policy.")
                        .resolvedAt(Instant.now().minus(12, ChronoUnit.HOURS))
                        .assignedEngineerId(assignedUser.getId())
                        .assignedEngineerName(assignedUser.getFirstName() + " " + assignedUser.getLastName())
                        .build()
        );

        List<Incident> saved = incidentRepository.saveAll(incidents);

        for (Incident inc : saved) {
            // Seed timeline event
            eventRepository.save(IncidentEvent.builder()
                    .incident(inc)
                    .eventType("ALERT_TRIGGER")
                    .eventDescription("Automated anomaly detection triggered " + inc.getIncidentNumber() + " (" + inc.getSeverity() + ")")
                    .actorName("FlowGuard Automated Alert Engine")
                    .createdAt(inc.getCreatedAt())
                    .build());

            // Seed comment
            commentRepository.save(IncidentComment.builder()
                    .incident(inc)
                    .userId(assignedUser.getId())
                    .userName("Sarah Chen")
                    .commentText("Investigating root cause traces and database metrics. Correlating logs with recent canary deployment.")
                    .createdAt(inc.getCreatedAt().plus(5, ChronoUnit.MINUTES))
                    .build());
        }
    }

    private void seedAlerts(User engineerUser) {
        if (alertRuleRepository.count() > 0) {
            return;
        }

        log.info("Seeding 4 threshold alert rules and historical trigger events...");

        List<AlertRule> rules = List.of(
                AlertRule.builder()
                        .name("Payment Gateway Latency Spike (>1500ms)")
                        .description("Triggers when P95 response time exceeds 1500ms within a 5-minute rolling window")
                        .targetType(TargetType.API_MONITOR)
                        .metricType(MetricType.RESPONSE_TIME)
                        .comparisonOperator(ComparisonOperator.GREATER_THAN)
                        .thresholdValue(1500.0)
                        .evaluationWindowMinutes(5)
                        .severity(IncidentSeverity.CRITICAL)
                        .isEnabled(true)
                        .build(),

                AlertRule.builder()
                        .name("User Authentication High Error Rate (>2%)")
                        .description("Triggers when 4xx/5xx responses exceed 2% of total auth traffic")
                        .targetType(TargetType.SERVICE)
                        .metricType(MetricType.ERROR_RATE)
                        .comparisonOperator(ComparisonOperator.GREATER_THAN)
                        .thresholdValue(2.0)
                        .evaluationWindowMinutes(5)
                        .severity(IncidentSeverity.HIGH)
                        .isEnabled(true)
                        .build(),

                AlertRule.builder()
                        .name("Inventory Service Availability Degradation (<99%)")
                        .description("Triggers when health check probe success rate falls below 99%")
                        .targetType(TargetType.API_MONITOR)
                        .metricType(MetricType.AVAILABILITY)
                        .comparisonOperator(ComparisonOperator.LESS_THAN)
                        .thresholdValue(99.0)
                        .evaluationWindowMinutes(10)
                        .severity(IncidentSeverity.MEDIUM)
                        .isEnabled(true)
                        .build(),

                AlertRule.builder()
                        .name("Downstream Consecutive Failure Spikes (>5)")
                        .description("Triggers when any microservice registers 5 consecutive probe timeouts")
                        .targetType(TargetType.GLOBAL)
                        .metricType(MetricType.FAILURE_COUNT)
                        .comparisonOperator(ComparisonOperator.GREATER_THAN)
                        .thresholdValue(5.0)
                        .evaluationWindowMinutes(3)
                        .severity(IncidentSeverity.HIGH)
                        .isEnabled(true)
                        .build()
        );

        List<AlertRule> savedRules = alertRuleRepository.saveAll(rules);

        AlertRule r1 = savedRules.get(0);
        AlertRule r2 = savedRules.get(1);

        alertHistoryRepository.save(AlertHistory.builder()
                .alertRuleId(r1.getId())
                .alertRuleName(r1.getName())
                .triggeredValue(2180.0)
                .message("Payment Gateway API latency reached 2,180ms (Threshold: 1,500ms)")
                .severity(IncidentSeverity.CRITICAL)
                .isAcknowledged(false)
                .triggeredAt(Instant.now().minus(14, ChronoUnit.MINUTES))
                .build());

        alertHistoryRepository.save(AlertHistory.builder()
                .alertRuleId(r2.getId())
                .alertRuleName(r2.getName())
                .triggeredValue(4.8)
                .message("User Authentication Service error rate reached 4.8% (Threshold: 2.0%)")
                .severity(IncidentSeverity.HIGH)
                .isAcknowledged(true)
                .acknowledgedBy(engineerUser.getId())
                .acknowledgedByName("Sarah Chen")
                .acknowledgedAt(Instant.now().minus(20, ChronoUnit.MINUTES))
                .triggeredAt(Instant.now().minus(35, ChronoUnit.MINUTES))
                .build());

        alertHistoryRepository.save(AlertHistory.builder()
                .alertRuleId(r1.getId())
                .alertRuleName(r1.getName())
                .triggeredValue(1840.0)
                .message("Payment Gateway API latency reached 1,840ms (Threshold: 1,500ms)")
                .severity(IncidentSeverity.CRITICAL)
                .isAcknowledged(true)
                .acknowledgedBy(engineerUser.getId())
                .acknowledgedByName("Sarah Chen")
                .acknowledgedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .triggeredAt(Instant.now().minus(2, ChronoUnit.HOURS).minus(15, ChronoUnit.MINUTES))
                .build());
    }

    private void seedServices(UUID teamId) {
        if (serviceRepository.count() > 0) {
            return;
        }

        log.info("Seeding 6 microservices in topology registry...");

        List<ServiceEntity> services = List.of(
                ServiceEntity.builder()
                        .name("API Gateway")
                        .slug("api-gateway")
                        .description("Edge ingress router, SSL termination, and JWT token validation")
                        .environment("PRODUCTION")
                        .currentStatus(MonitorStatus.HEALTHY)
                        .teamId(teamId)
                        .build(),

                ServiceEntity.builder()
                        .name("Payment Service")
                        .slug("payment-service")
                        .description("Card authorization, settlement queues, and refund processing")
                        .environment("PRODUCTION")
                        .currentStatus(MonitorStatus.DEGRADED)
                        .teamId(teamId)
                        .build(),

                ServiceEntity.builder()
                        .name("Order Management Service")
                        .slug("order-service")
                        .description("Order ledger, checkout state machine, and customer invoicing")
                        .environment("PRODUCTION")
                        .currentStatus(MonitorStatus.HEALTHY)
                        .teamId(teamId)
                        .build(),

                ServiceEntity.builder()
                        .name("Inventory Service")
                        .slug("inventory-service")
                        .description("Warehouse SKU inventory tracking and stock reservation")
                        .environment("PRODUCTION")
                        .currentStatus(MonitorStatus.HEALTHY)
                        .teamId(teamId)
                        .build(),

                ServiceEntity.builder()
                        .name("Notification Service")
                        .slug("notification-service")
                        .description("Transactional emails, SMS dispatch, and customer webhooks")
                        .environment("STAGING")
                        .currentStatus(MonitorStatus.HEALTHY)
                        .teamId(teamId)
                        .build(),

                ServiceEntity.builder()
                        .name("Authentication Service")
                        .slug("auth-service")
                        .description("User identity management, OAuth2 token minting, and RBAC")
                        .environment("PRODUCTION")
                        .currentStatus(MonitorStatus.HEALTHY)
                        .teamId(teamId)
                        .build()
        );

        serviceRepository.saveAll(services);
    }

    private Role getOrCreateRole(RoleType roleType, String description) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(roleType)
                        .description(description)
                        .build()));
    }
}
