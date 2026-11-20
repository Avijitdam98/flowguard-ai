package com.flowguard.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Team slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase alphanumeric characters and hyphens")
    @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters")
    private String slug;

    private String description;
}

