package com.flowguard.serviceregistry;

import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.common.ApiResponse;
import com.flowguard.serviceregistry.dto.CreateServiceRequest;
import com.flowguard.serviceregistry.dto.ServiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Microservice Registry", description = "Endpoints for registering and querying distributed service architecture topologies")
public class ServiceController {

    private final ServiceRegistryService serviceRegistryService;

    @GetMapping
    @Operation(summary = "List all microservices in the topology")
    public ResponseEntity<ApiResponse<Page<ServiceDto>>> getAllServices(
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) MonitorStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ServiceDto> services = serviceRegistryService.getAllServices(environment, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Services retrieved successfully", services));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get microservice details by ID")
    public ResponseEntity<ApiResponse<ServiceDto>> getServiceById(@PathVariable UUID id) {
        ServiceDto service = serviceRegistryService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.ok("Service retrieved successfully", service));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Register a new microservice in the catalog")
    public ResponseEntity<ApiResponse<ServiceDto>> registerService(@Valid @RequestBody CreateServiceRequest request) {
        ServiceDto created = serviceRegistryService.registerService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Service registered successfully", created));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Remove a microservice from catalog")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable UUID id) {
        serviceRegistryService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.ok("Service removed successfully", null));
    }
}

