package com.flowguard.incident.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {

    @NotBlank(message = "Comment text cannot be empty")
    private String commentText;
}

