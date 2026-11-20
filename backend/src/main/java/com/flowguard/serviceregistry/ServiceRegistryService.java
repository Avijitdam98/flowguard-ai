package com.flowguard.serviceregistry;

import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.serviceregistry.dto.CreateServiceRequest;
import com.flowguard.serviceregistry.dto.ServiceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ServiceRegistryService {
    Page<ServiceDto> getAllServices(String environment, MonitorStatus status, Pageable pageable);
    ServiceDto getServiceById(UUID id);
    ServiceDto getServiceBySlug(String slug, String environment);
    ServiceDto registerService(CreateServiceRequest request);
    void deleteService(UUID id);
}

