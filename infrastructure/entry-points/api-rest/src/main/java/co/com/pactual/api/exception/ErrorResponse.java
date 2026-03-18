package co.com.pactual.api.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        String code,
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId
) {
}
