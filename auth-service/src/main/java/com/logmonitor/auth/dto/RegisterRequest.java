package com.logmonitor.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Schema(example = "alice")
    private String username;

    @NotBlank
    @Size(min = 6, message = "password must be at least 6 characters")
    @Schema(example = "password123")
    private String password;

    @Schema(example = "USER", description = "USER or ADMIN")
    private String role = "USER";
}
