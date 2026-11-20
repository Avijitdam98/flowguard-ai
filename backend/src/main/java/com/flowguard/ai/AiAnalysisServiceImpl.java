package com.flowguard.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowguard.ai.dto.AiChatRequestDto;
import com.flowguard.ai.dto.AiChatResponseDto;
import com.flowguard.ai.dto.AiRootCauseResponseDto;
import com.flowguard.ai.provider.AiProvider;
import com.flowguard.ai.provider.AiProviderManager;
import com.flowguard.apimonitor.ApiMonitor;
import com.flowguard.apimonitor.ApiMonitorRepository;
import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.incident.Incident;
import com.flowguard.incident.IncidentRepository;
import com.flowguard.incident.IncidentStatus;
import com.flowguard.logmanagement.ApplicationLog;
import com.flowguard.logmanagement.ApplicationLogRepository;
import com.flowguard.logmanagement.LogLevel;
import com.flowguard.serviceregistry.ServiceRepository;
import com.flowguard.transaction.Transaction;
import com.flowguard.transaction.TransactionRepository;
import com.flowguard.transaction.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisServiceImpl.class);

    private final IncidentRepository incidentRepository;
    private final ApplicationLogRepository logRepository;
    private final TransactionRepository transactionRepository;
    private final ApiMonitorRepository monitorRepository;
    private final ServiceRepository serviceRepository;
    private final AiProviderManager aiProviderManager;
    private final ObjectMapper objectMapper;

    private static final Pattern PII_BEARER_PATTERN = Pattern.compile("(?i)bearer\\s+[a-zA-Z0-9_\\-\\.]+");
    private static final Pattern PII_PASSWORD_PATTERN = Pattern.compile("(?i)(password|secret|token)[\"']?\\s*[:=]\\s*[\"']?[^\"',\\s}]+");

    @Override
    public AiRootCauseResponseDto analyzeIncident(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        // Resolve service name if affectedServiceId is present
        String serviceName = "Unknown";
        if (incident.getAffectedServiceId() != null) {
            serviceName = serviceRepository.findById(incident.getAffectedServiceId())
                    .map(com.flowguard.serviceregistry.ServiceEntity::getName)
                    .orElse("Service-" + incident.getAffectedServiceId().toString().substring(0, 8));
        } else {
            serviceName = incident.getTitle().split(" ")[0];
        }

        // Fetch real related telemetry from database
        List<ApplicationLog> relatedLogs = logRepository.searchLogs(
                null, null, serviceName, null, null, PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();

        List<Transaction> failedTransactions = transactionRepository.findByTransactionStatus(
                TransactionStatus.FAILED, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "startTime"))
        ).getContent();

        // Sanitize telemetry
        String sanitizedDescription = sanitizeTelemetry(incident.getDescription());
        List<String> realErrorSnippets = relatedLogs.stream()
                .map(l -> "[" + l.getLogLevel() + "] " + sanitizeTelemetry(l.getMessage()))
                .limit(5)
                .collect(Collectors.toList());

        Optional<AiProvider> activeProvider = aiProviderManager.getActiveProvider();

        if (activeProvider.isEmpty()) {
            // Ollama / AI Provider is offline - DO NOT generate fake analysis!
            String model = aiProviderManager.getOllamaProvider().getModelName();
            String baseUrl = aiProviderManager.getOllamaProvider().getBaseUrl();

            List<String> affected = new ArrayList<>();
            if (!"Unknown".equalsIgnoreCase(serviceName)) {
                affected.add(serviceName);
            }

            return AiRootCauseResponseDto.builder()
                    .incidentId(incident.getId())
                    .incidentNumber(incident.getIncidentNumber())
                    .summary("FlowGuard AI is configured to use local open-source LLM via Ollama (" + model + "), but the service is currently unreachable at " + baseUrl + ". Start Ollama locally with `ollama run " + model + "` to enable automated LLM root cause analysis.")
                    .confidenceScore(0.0)
                    .potentialRootCauses(List.of("AI Root Cause Analysis requires a running local Ollama instance (model: " + model + ") or an active AI provider."))
                    .affectedServices(affected)
                    .relevantErrorPatterns(realErrorSnippets.isEmpty() ? List.of("No matching database error logs found for keyword: " + serviceName) : realErrorSnippets)
                    .recommendedInvestigationSteps(List.of(
                            "Start local Ollama server: run `ollama serve` and `ollama run " + model + "`",
                            "Verify Ollama health check: curl " + baseUrl + "/api/tags",
                            "Inspect centralized logs directly in FlowGuard Logs Explorer"
                    ))
                    .suggestedRemediationActions(Collections.emptyList())
                    .supportingEvidence("Collected " + relatedLogs.size() + " related database logs and " + failedTransactions.size() + " recent failed transactions.")
                    .modelUsed("Ollama (" + model + ") [OFFLINE]")
                    .disclaimer("Ollama is currently offline. No automated hallucinated data was generated. Please launch Ollama to enable AI synthesis.")
                    .build();
        }

        AiProvider provider = activeProvider.get();
        log.info("Generating AI Root Cause Analysis using provider: {} (model: {})", provider.getProviderName(), provider.getModelName());

        try {
            String systemPrompt = "You are FlowGuard AI, a Principal Site Reliability Engineer and Observability expert. "
                    + "Analyze the incident details and related application telemetry provided. "
                    + "Respond ONLY with a valid JSON object without markdown code blocks, conforming to this exact schema:\n"
                    + "{\n"
                    + "  \"summary\": \"string\",\n"
                    + "  \"confidenceScore\": 0.85,\n"
                    + "  \"potentialRootCauses\": [\"cause 1\", \"cause 2\"],\n"
                    + "  \"affectedServices\": [\"service1\"],\n"
                    + "  \"relevantErrorPatterns\": [\"pattern 1\"],\n"
                    + "  \"recommendedInvestigationSteps\": [\"step 1\", \"step 2\"],\n"
                    + "  \"suggestedRemediationActions\": [\"action 1\", \"action 2\"],\n"
                    + "  \"supportingEvidence\": \"string\"\n"
                    + "}";

            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("Incident Number: ").append(incident.getIncidentNumber()).append("\n");
            userPrompt.append("Title: ").append(incident.getTitle()).append("\n");
            userPrompt.append("Severity: ").append(incident.getSeverity()).append("\n");
            userPrompt.append("Description: ").append(sanitizedDescription).append("\n");
            userPrompt.append("Affected Service: ").append(serviceName).append("\n");

            userPrompt.append("\nRelated Application Logs from Database:\n");
            if (relatedLogs.isEmpty()) {
                userPrompt.append("None available.\n");
            } else {
                for (ApplicationLog logEntry : relatedLogs) {
                    userPrompt.append("- [").append(logEntry.getLogLevel()).append("] ").append(sanitizeTelemetry(logEntry.getMessage())).append("\n");
                }
            }

            userPrompt.append("\nRecent Failed Transactions:\n");
            if (failedTransactions.isEmpty()) {
                userPrompt.append("None reported.\n");
            } else {
                for (Transaction tx : failedTransactions) {
                    userPrompt.append("- TX: ").append(tx.getTransactionId()).append(" (Duration: ").append(tx.getTotalDurationMs()).append("ms, Reason: ").append(tx.getFailureReason()).append(")\n");
                }
            }

            String rawResponse = provider.generate(systemPrompt, userPrompt.toString());
            return parseAiRcaResponse(incident, rawResponse, provider.getModelName(), serviceName);
        } catch (Exception ex) {
            log.error("Error generating RCA with provider {}: {}", provider.getProviderName(), ex.getMessage());
            return AiRootCauseResponseDto.builder()
                    .incidentId(incident.getId())
                    .incidentNumber(incident.getIncidentNumber())
                    .summary("AI Root Cause Analysis query failed: " + ex.getMessage())
                    .confidenceScore(0.0)
                    .potentialRootCauses(List.of("Error communicating with AI model: " + ex.getMessage()))
                    .affectedServices(List.of(serviceName))
                    .relevantErrorPatterns(realErrorSnippets)
                    .recommendedInvestigationSteps(List.of("Check AI provider logs and verify network connectivity"))
                    .suggestedRemediationActions(Collections.emptyList())
                    .supportingEvidence("Analysis aborted due to AI model execution error.")
                    .modelUsed(provider.getModelName() + " [ERROR]")
                    .disclaimer("An error occurred during AI analysis. Please review telemetry manually.")
                    .build();
        }
    }

    @Override
    public AiChatResponseDto chatWithCopilot(AiChatRequestDto request) {
        String query = sanitizeTelemetry(request.getPrompt()).trim();
        log.info("FlowGuard AI Copilot query: {}", query);

        // Fetch real telemetry snapshots from database
        long activeIncidents = incidentRepository.countByStatus(IncidentStatus.INVESTIGATING)
                + incidentRepository.countByStatus(IncidentStatus.DETECTED);
        List<Incident> recentIncidents = incidentRepository.findAll(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        List<ApiMonitor> allMonitors = monitorRepository.findAll();
        List<Transaction> failedTxs = transactionRepository.findByTransactionStatus(
                TransactionStatus.FAILED, PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "startTime"))
        ).getContent();
        List<ApplicationLog> errorLogs = logRepository.findByLogLevel(
                LogLevel.ERROR, PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();

        List<String> entities = new ArrayList<>();
        recentIncidents.forEach(i -> entities.add(i.getIncidentNumber() + ": " + i.getTitle()));
        failedTxs.forEach(t -> entities.add("TX-" + t.getTransactionId().substring(0, Math.min(8, t.getTransactionId().length()))));

        Optional<AiProvider> activeProvider = aiProviderManager.getActiveProvider();

        if (activeProvider.isEmpty()) {
            // Ollama / AI is offline - return dynamic DB snapshot rather than fake hallucinated text
            String model = aiProviderManager.getOllamaProvider().getModelName();
            String baseUrl = aiProviderManager.getOllamaProvider().getBaseUrl();

            StringBuilder fallback = new StringBuilder();
            fallback.append("### FlowGuard AI Copilot (Ollama: `").append(model).append("` - Offline)\n\n");
            fallback.append("The local AI provider at `").append(baseUrl).append("` is currently unreachable.\n");
            fallback.append("To enable real-time conversational analysis, start Ollama with:\n");
            fallback.append("```bash\nollama serve\nollama run ").append(model).append("\n```\n\n");
            fallback.append("### Real-Time Live Telemetry Snapshot (from PostgreSQL):\n\n");
            fallback.append("- **Active Open Incidents:** ").append(activeIncidents).append("\n");
            fallback.append("- **Registered Monitored APIs:** ").append(allMonitors.size()).append("\n");
            fallback.append("- **Recent Failed Transactions:** ").append(failedTxs.size()).append("\n");
            fallback.append("- **Recent Database Error Logs:** ").append(errorLogs.size()).append("\n\n");

            if (!recentIncidents.isEmpty()) {
                fallback.append("**Latest Active Incidents:**\n");
                for (Incident inc : recentIncidents) {
                    fallback.append("- `").append(inc.getIncidentNumber()).append("` (").append(inc.getSeverity()).append("): ").append(inc.getTitle()).append("\n");
                }
            }

            return AiChatResponseDto.builder()
                    .response(fallback.toString())
                    .model("Ollama (" + model + ") [OFFLINE]")
                    .referencedEntities(entities)
                    .suggestedNextPrompts(List.of(
                            "Start local Ollama server (`ollama serve`)",
                            "Pull recommended model: `ollama run " + model + "`",
                            "Show active incidents and MTTR summary"
                    ))
                    .generatedAt(Instant.now())
                    .safetyNotice("Generated from real-time PostgreSQL telemetry. Local Ollama LLM is currently offline.")
                    .build();
        }

        AiProvider provider = activeProvider.get();

        try {
            String systemPrompt = "You are the FlowGuard AI Observability Copilot, an enterprise assistant for SREs and developers. "
                    + "You have access to real-time telemetry from the PostgreSQL database provided below. "
                    + "Answer the user's question clearly, concisely, and accurately based strictly on this real data. "
                    + "Do not fabricate fake incidents, logs, or metrics. Format using clean GitHub-style Markdown.";

            StringBuilder contextPrompt = new StringBuilder();
            contextPrompt.append("Real-Time Telemetry Context:\n");
            contextPrompt.append("- Total Registered API Monitors: ").append(allMonitors.size()).append("\n");
            contextPrompt.append("- Active Open Incidents: ").append(activeIncidents).append("\n");

            if (!recentIncidents.isEmpty()) {
                contextPrompt.append("\nRecent Incidents in System:\n");
                for (Incident inc : recentIncidents) {
                    contextPrompt.append("- ").append(inc.getIncidentNumber()).append(" [").append(inc.getSeverity()).append("] [").append(inc.getStatus()).append("]: ").append(inc.getTitle()).append(" - ").append(inc.getDescription()).append("\n");
                }
            }

            if (!allMonitors.isEmpty()) {
                contextPrompt.append("\nRegistered API Monitors:\n");
                for (ApiMonitor m : allMonitors) {
                    contextPrompt.append("- ").append(m.getName()).append(" (").append(m.getHttpMethod()).append(" ").append(m.getEndpointPath()).append("): Status=").append(m.getCurrentStatus()).append("\n");
                }
            }

            if (!failedTxs.isEmpty()) {
                contextPrompt.append("\nRecent Failed Transactions:\n");
                for (Transaction tx : failedTxs) {
                    contextPrompt.append("- ID: ").append(tx.getTransactionId()).append(", Reason: ").append(tx.getFailureReason()).append("\n");
                }
            }

            contextPrompt.append("\nUser Question: ").append(query);

            String answer = provider.generate(systemPrompt, contextPrompt.toString());

            List<String> nextPrompts = List.of(
                    "Which API monitor has the highest latency?",
                    "Summarize open incidents and potential root causes",
                    "Show recent error logs across services"
            );

            return AiChatResponseDto.builder()
                    .response(answer)
                    .model(provider.getModelName())
                    .referencedEntities(entities)
                    .suggestedNextPrompts(nextPrompts)
                    .generatedAt(Instant.now())
                    .safetyNotice("Generated by FlowGuard AI (" + provider.getProviderName() + "). Recommendations should be verified before applying production changes.")
                    .build();
        } catch (Exception ex) {
            log.error("AI Copilot chat failed: {}", ex.getMessage());
            return AiChatResponseDto.builder()
                    .response("Error communicating with AI Copilot: " + ex.getMessage() + ". Please ensure Ollama or your AI provider is active.")
                    .model(provider.getModelName() + " [ERROR]")
                    .referencedEntities(entities)
                    .suggestedNextPrompts(List.of("Check Ollama status", "Retry question"))
                    .generatedAt(Instant.now())
                    .safetyNotice("AI communication error.")
                    .build();
        }
    }

    private AiRootCauseResponseDto parseAiRcaResponse(Incident incident, String rawResponse, String modelName, String fallbackService) {
        try {
            // Strip markdown code fences if model enclosed JSON in ```json ... ```
            String cleaned = rawResponse.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            JsonNode node = objectMapper.readTree(cleaned);

            String summary = node.has("summary") ? node.get("summary").asText() : "Incident RCA generated by AI model";
            double confidence = node.has("confidenceScore") ? node.get("confidenceScore").asDouble(0.85) : 0.85;

            List<String> rootCauses = extractStringList(node, "potentialRootCauses");
            List<String> affectedServices = extractStringList(node, "affectedServices");
            if (affectedServices.isEmpty() && fallbackService != null && !"Unknown".equalsIgnoreCase(fallbackService)) {
                affectedServices.add(fallbackService);
            }
            List<String> errorPatterns = extractStringList(node, "relevantErrorPatterns");
            List<String> steps = extractStringList(node, "recommendedInvestigationSteps");
            List<String> remediation = extractStringList(node, "suggestedRemediationActions");
            String evidence = node.has("supportingEvidence") ? node.get("supportingEvidence").asText() : "";

            return AiRootCauseResponseDto.builder()
                    .incidentId(incident.getId())
                    .incidentNumber(incident.getIncidentNumber())
                    .summary(summary)
                    .confidenceScore(confidence)
                    .potentialRootCauses(rootCauses)
                    .affectedServices(affectedServices)
                    .relevantErrorPatterns(errorPatterns)
                    .recommendedInvestigationSteps(steps)
                    .suggestedRemediationActions(remediation)
                    .supportingEvidence(evidence)
                    .modelUsed(modelName)
                    .disclaimer("Generated by FlowGuard AI Observability Engine. Recommendations should be validated by on-call engineering before applying configuration changes.")
                    .build();
        } catch (Exception ex) {
            log.warn("Could not parse AI response as JSON, wrapping raw text: {}", ex.getMessage());
            return AiRootCauseResponseDto.builder()
                    .incidentId(incident.getId())
                    .incidentNumber(incident.getIncidentNumber())
                    .summary(rawResponse)
                    .confidenceScore(0.75)
                    .potentialRootCauses(List.of("Analysis synthesized by LLM"))
                    .affectedServices(fallbackService != null ? List.of(fallbackService) : Collections.emptyList())
                    .relevantErrorPatterns(Collections.emptyList())
                    .recommendedInvestigationSteps(List.of("Review incident telemetry and logs"))
                    .suggestedRemediationActions(Collections.emptyList())
                    .supportingEvidence(rawResponse)
                    .modelUsed(modelName)
                    .disclaimer("Generated by FlowGuard AI Observability Engine.")
                    .build();
        }
    }

    private List<String> extractStringList(JsonNode node, String fieldName) {
        List<String> result = new ArrayList<>();
        if (node.has(fieldName) && node.get(fieldName).isArray()) {
            for (JsonNode item : node.get(fieldName)) {
                result.add(item.asText());
            }
        }
        return result;
    }

    private String sanitizeTelemetry(String input) {
        if (input == null) return "";
        String sanitized = PII_BEARER_PATTERN.matcher(input).replaceAll("Bearer [REDACTED_TOKEN]");
        sanitized = PII_PASSWORD_PATTERN.matcher(sanitized).replaceAll("$1=[REDACTED]");
        return sanitized;
    }
}
