package org.example.resume;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ResumeRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Resume text is required")
    private String rawText;
}