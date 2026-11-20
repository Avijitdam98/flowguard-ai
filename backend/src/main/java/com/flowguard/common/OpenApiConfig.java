package com.flowguard.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI flowGuardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowGuard AI - Intelligent API Monitoring & Incident Management Platform")
                        .description("Production-grade enterprise REST APIs for distributed transaction intelligence, API monitoring, centralized logs, AI root-cause analysis, and incident management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FlowGuard Engineering Team")
                                .email("engineering@flowguard.ai")
                                .url("https://flowguard.ai"))
                        .license(new License().name("Proprietary Enterprise License")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token to authorize requests.")));
    }
}

