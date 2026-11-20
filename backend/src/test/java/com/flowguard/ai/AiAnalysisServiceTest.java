package com.flowguard.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowguard.ai.dto.AiChatRequestDto;
import com.flowguard.ai.dto.AiChatResponseDto;
import com.flowguard.ai.dto.AiRootCauseResponseDto;
import com.flowguard.ai.provider.AiProvider;
import com.flowguard.ai.provider.AiProviderManager;
import com.flowguard.ai.provider.OllamaAiProvider;
import com.flowguard.apimonitor.ApiMonitorRepository;
import com.flowguard.incident.Incident;
import com.flowguard.incident.IncidentRepository;
import com.flowguard.incident.IncidentSeverity;
import com.flowguard.incident.IncidentStatus;
import com.flowguard.logmanagement.ApplicationLogRepository;
import com.flowguard.serviceregistry.ServiceRepository;
import com.flowguard.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private ApplicationLogRepository logRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ApiMonitorRepository monitorRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AiProviderManager aiProviderManager;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiAnalysisServiceImpl aiService;

    private Incident sampleIncident;
    private OllamaAiProvider mockOllama;

    @BeforeEach
    void setUp() {
        sampleIncident = Incident.builder()
                .incidentNumber("INC-2026-001")
                .title("504 Gateway Timeout in Payment Processing")
                .description("Elevated latency on POST /api/v1/payments")
                .severity(IncidentSeverity.CRITICAL)
                .status(IncidentStatus.INVESTIGATING)
                .build();
        sampleIncident.setId(UUID.randomUUID());

        mockOllama = mock(OllamaAiProvider.class);
    }

    @Test
    @DisplayName("Should return clean offline status without fake data when Ollama is offline")
    void testAnalyzeIncidentWhenAiOffline() {
        when(incidentRepository.findById(sampleIncident.getId())).thenReturn(Optional.of(sampleIncident));
        when(logRepository.searchLogs(isNull(), isNull(), anyString(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(transactionRepository.findByTransactionStatus(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(aiProviderManager.getActiveProvider()).thenReturn(Optional.empty());
        when(aiProviderManager.getOllamaProvider()).thenReturn(mockOllama);
        when(mockOllama.getModelName()).thenReturn("qwen2.5:7b");
        when(mockOllama.getBaseUrl()).thenReturn("http://localhost:11434");

        AiRootCauseResponseDto response = aiService.analyzeIncident(sampleIncident.getId());

        assertNotNull(response);
        assertEquals(0.0, response.getConfidenceScore());
        assertTrue(response.getSummary().contains("currently unreachable"));
        assertTrue(response.getModelUsed().contains("OFFLINE"));
    }

    @Test
    @DisplayName("Should parse structured JSON RCA when AI provider is active")
    void testAnalyzeIncidentWithActiveProvider() {
        AiProvider mockProvider = mock(AiProvider.class);
        when(incidentRepository.findById(sampleIncident.getId())).thenReturn(Optional.of(sampleIncident));
        when(logRepository.searchLogs(isNull(), isNull(), anyString(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(transactionRepository.findByTransactionStatus(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(aiProviderManager.getActiveProvider()).thenReturn(Optional.of(mockProvider));
        when(mockProvider.getModelName()).thenReturn("qwen2.5:7b");
        when(mockProvider.generate(anyString(), anyString())).thenReturn("{\n"
                + "  \"summary\": \"Socket timeout connecting to payment gateway connector\",\n"
                + "  \"confidenceScore\": 0.92,\n"
                + "  \"potentialRootCauses\": [\"Database connection pool exhausted\"],\n"
                + "  \"affectedServices\": [\"payment-service\"],\n"
                + "  \"relevantErrorPatterns\": [\"SocketTimeoutException\"],\n"
                + "  \"recommendedInvestigationSteps\": [\"Inspect connection pool\"],\n"
                + "  \"suggestedRemediationActions\": [\"Scale pool size\"],\n"
                + "  \"supportingEvidence\": \"Telemetry confirms 504 timeouts\"\n"
                + "}");

        AiRootCauseResponseDto response = aiService.analyzeIncident(sampleIncident.getId());

        assertNotNull(response);
        assertEquals(0.92, response.getConfidenceScore());
        assertEquals("Socket timeout connecting to payment gateway connector", response.getSummary());
        assertEquals("qwen2.5:7b", response.getModelUsed());
        assertEquals(1, response.getPotentialRootCauses().size());
        assertEquals("Database connection pool exhausted", response.getPotentialRootCauses().get(0));
    }

    @Test
    @DisplayName("Should answer copilot question using real database telemetry when AI provider is active")
    void testChatWithCopilotActiveProvider() {
        AiProvider mockProvider = mock(AiProvider.class);
        when(aiProviderManager.getActiveProvider()).thenReturn(Optional.of(mockProvider));
        when(mockProvider.getModelName()).thenReturn("qwen2.5:7b");
        when(mockProvider.getProviderName()).thenReturn("Ollama (Local Open-Source)");
        when(mockProvider.generate(anyString(), anyString())).thenReturn("Payment service experienced socket timeouts due to connection pool saturation.");
        when(incidentRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(sampleIncident)));
        when(monitorRepository.findAll()).thenReturn(Collections.emptyList());
        when(transactionRepository.findByTransactionStatus(any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(logRepository.findByLogLevel(any(), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        AiChatRequestDto request = new AiChatRequestDto("Why did payment fail?", null, null);
        AiChatResponseDto response = aiService.chatWithCopilot(request);

        assertNotNull(response);
        assertEquals("Payment service experienced socket timeouts due to connection pool saturation.", response.getResponse());
        assertEquals("qwen2.5:7b", response.getModel());
    }
}
