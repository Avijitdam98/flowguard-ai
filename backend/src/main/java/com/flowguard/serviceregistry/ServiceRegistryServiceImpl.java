package com.flowguard.serviceregistry;

import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.common.BadRequestException;
import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.serviceregistry.dto.CreateServiceRequest;
import com.flowguard.serviceregistry.dto.ServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceRegistryServiceImpl implements ServiceRegistryService {

    private final ServiceRepository serviceRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceDto> getAllServices(String environment, MonitorStatus status, Pageable pageable) {
        Page<ServiceEntity> page;
        if (environment != null && !environment.isBlank() && !environment.equalsIgnoreCase("ALL")) {
            page = serviceRepository.findByEnvironment(environment, pageable);
        } else if (status != null) {
            page = serviceRepository.findByCurrentStatus(status, pageable);
        } else {
            page = serviceRepository.findAll(pageable);
        }
        return page.map(ServiceDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDto getServiceById(UUID id) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        return ServiceDto.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDto getServiceBySlug(String slug, String environment) {
        String env = (environment != null && !environment.isBlank()) ? environment : "PRODUCTION";
        ServiceEntity entity = serviceRepository.findBySlugAndEnvironment(slug, env)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "slug", slug));
        return ServiceDto.fromEntity(entity);
    }

    @Override
    @Transactional
    public ServiceDto registerService(CreateServiceRequest request) {
        String env = request.getEnvironment() != null ? request.getEnvironment() : "PRODUCTION";
        if (serviceRepository.findBySlugAndEnvironment(request.getSlug(), env).isPresent()) {
            throw new BadRequestException("Service with slug '" + request.getSlug() + "' in environment '" + env + "' already exists");
        }

        ServiceEntity entity = ServiceEntity.builder()
                .teamId(request.getTeamId())
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .environment(env)
                .currentStatus(request.getCurrentStatus() != null ? request.getCurrentStatus() : MonitorStatus.HEALTHY)
                .build();

        ServiceEntity saved = serviceRepository.save(entity);
        return ServiceDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteService(UUID id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service", "id", id);
        }
        serviceRepository.deleteById(id);
    }
}

