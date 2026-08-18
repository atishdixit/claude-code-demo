package com.logmonitor.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Schema(example = "alice")
    private String username;

    @NotBlank
    @Schema(example = "password123")
    private String password;
}
