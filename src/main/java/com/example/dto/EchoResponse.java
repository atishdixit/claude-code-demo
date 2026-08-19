package com.example.dto;

import java.time.Instant;

public record EchoResponse(String original, String upper, int length, Instant receivedAt) {
}
