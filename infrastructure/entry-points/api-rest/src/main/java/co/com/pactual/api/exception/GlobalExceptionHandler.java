package co.com.pactual.api.exception;

import co.com.pactual.api.config.CorrelationIdFilter;
import co.com.pactual.usecase.getfunds.exception.FundsRetrievalException;
import co.com.pactual.usecase.gettransactionhistory.exception.TransactionHistoryRetrievalException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionAlreadyCancelledException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionCancellationPersistenceException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.ActiveSubscriptionAlreadyExistsException;
import co.com.pactual.usecase.subscribefund.exception.ClientNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.FundNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.InsufficientBalanceException;
import co.com.pactual.usecase.subscribefund.exception.MinimumSubscriptionAmountException;
import co.com.pactual.usecase.subscribefund.exception.SubscriptionPersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.BAD_REQUEST);
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFound(
            ClientNotFoundException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.NOT_FOUND);
        return buildResponse(HttpStatus.NOT_FOUND, "CLIENT_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(FundNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFundNotFound(
            FundNotFoundException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.NOT_FOUND);
        return buildResponse(HttpStatus.NOT_FOUND, "FUND_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.BAD_REQUEST);
        return buildResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", exception.getMessage(), request);
    }

    @ExceptionHandler(ActiveSubscriptionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveSubscriptionAlreadyExists(
            ActiveSubscriptionAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.CONFLICT);
        return buildResponse(HttpStatus.CONFLICT, "ACTIVE_SUBSCRIPTION_ALREADY_EXISTS", exception.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionNotFound(
            SubscriptionNotFoundException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.NOT_FOUND);
        return buildResponse(HttpStatus.NOT_FOUND, "SUBSCRIPTION_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionAlreadyCancelled(
            SubscriptionAlreadyCancelledException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.BAD_REQUEST);
        return buildResponse(HttpStatus.BAD_REQUEST, "SUBSCRIPTION_ALREADY_CANCELLED", exception.getMessage(), request);
    }

    @ExceptionHandler(MinimumSubscriptionAmountException.class)
    public ResponseEntity<ErrorResponse> handleMinimumSubscriptionAmount(
            MinimumSubscriptionAmountException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.BAD_REQUEST);
        return buildResponse(HttpStatus.BAD_REQUEST, "MINIMUM_SUBSCRIPTION_AMOUNT", exception.getMessage(), request);
    }

    @ExceptionHandler(FundsRetrievalException.class)
    public ResponseEntity<ErrorResponse> handleFundsRetrieval(
            FundsRetrievalException exception,
            HttpServletRequest request
    ) {
        logError(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "FUNDS_RETRIEVAL_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(TransactionHistoryRetrievalException.class)
    public ResponseEntity<ErrorResponse> handleTransactionHistoryRetrieval(
            TransactionHistoryRetrievalException exception,
            HttpServletRequest request
    ) {
        logError(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "TRANSACTION_HISTORY_RETRIEVAL_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionPersistenceException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionPersistence(
            SubscriptionPersistenceException exception,
            HttpServletRequest request
    ) {
        logError(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SUBSCRIPTION_PERSISTENCE_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(SubscriptionCancellationPersistenceException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionCancellationPersistence(
            SubscriptionCancellationPersistenceException exception,
            HttpServletRequest request
    ) {
        logError(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SUBSCRIPTION_CANCELLATION_PERSISTENCE_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.BAD_REQUEST);
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_PAYLOAD", "Invalid request payload", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        logWarn(exception, request, HttpStatus.BAD_REQUEST, message);
        return buildResponse(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        logWarn(exception, request, HttpStatus.BAD_REQUEST);
        return buildResponse(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", exception.getParameterName() + " is required", request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        String message = exception.getParameterValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid request parameter")
                .collect(Collectors.joining(", "));

        logWarn(exception, request, HttpStatus.BAD_REQUEST, message);
        return buildResponse(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        logError(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected internal error", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .correlationId(resolveCorrelationId(request))
                        .build());
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private void logWarn(Exception exception, HttpServletRequest request, HttpStatus status) {
        logWarn(exception, request, status, exception.getMessage());
    }

    private void logWarn(Exception exception, HttpServletRequest request, HttpStatus status, String message) {
        LOGGER.warn(
                "Handled request with controlled error. method={} path={} status={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                message
        );
    }

    private void logError(Exception exception, HttpServletRequest request, HttpStatus status) {
        LOGGER.error(
                "Handled request with unexpected error. method={} path={} status={}",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                exception
        );
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String headerCorrelationId = request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        if (headerCorrelationId != null && !headerCorrelationId.isBlank()) {
            return headerCorrelationId.trim();
        }

        String mdcCorrelationId = MDC.get("correlationId");
        if (mdcCorrelationId != null && !mdcCorrelationId.isBlank()) {
            return mdcCorrelationId;
        }

        return UUID.randomUUID().toString();
    }
}
