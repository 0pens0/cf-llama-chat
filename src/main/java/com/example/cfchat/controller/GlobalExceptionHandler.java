package com.example.cfchat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final Path DEBUG_LOG_PATH = Path.of("/Users/orenpenso/git/cf-llama-chat/.cursor/debug-9421df.log");
    private static final ObjectMapper DEBUG_MAPPER = new ObjectMapper();

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        log.error("Server error: {}", ex.getMessage());
        // region agent log
        appendDebugNdjson("H4", "IllegalStateException", Map.of(
                "exceptionType", ex.getClass().getName(),
                "message", ex.getMessage() != null ? ex.getMessage() : ""
        ));
        // endregion
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        // region agent log
        appendDebugNdjson("H4", "General exception", Map.of(
                "exceptionType", ex.getClass().getName(),
                "message", ex.getMessage() != null ? ex.getMessage() : ""
        ));
        // endregion
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "An unexpected error occurred"));
    }

    private static void appendDebugNdjson(String hypothesisId, String message, Map<String, Object> data) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sessionId", "9421df");
            payload.put("runId", "pre-fix-1");
            payload.put("hypothesisId", hypothesisId);
            payload.put("location", "GlobalExceptionHandler");
            payload.put("message", message);
            payload.put("data", data);
            payload.put("timestamp", System.currentTimeMillis());
            Files.writeString(
                    DEBUG_LOG_PATH,
                    DEBUG_MAPPER.writeValueAsString(payload) + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
        }
    }
}
