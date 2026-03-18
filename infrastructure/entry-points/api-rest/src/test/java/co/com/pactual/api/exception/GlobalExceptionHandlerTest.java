package co.com.pactual.api.exception;

import co.com.pactual.api.config.CorrelationIdFilter;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionAlreadyCancelledException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionCancellationPersistenceException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionNotFoundException;
import co.com.pactual.usecase.getfunds.exception.FundsRetrievalException;
import co.com.pactual.usecase.gettransactionhistory.exception.TransactionHistoryRetrievalException;
import co.com.pactual.usecase.subscribefund.exception.ActiveSubscriptionAlreadyExistsException;
import co.com.pactual.usecase.subscribefund.exception.ClientNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.FundNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.InsufficientBalanceException;
import co.com.pactual.usecase.subscribefund.exception.MinimumSubscriptionAmountException;
import co.com.pactual.usecase.subscribefund.exception.SubscriptionPersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldUseHeaderCorrelationIdWhenHandlingControlledError() {
        MockHttpServletRequest request = request("POST", "/subscriptions");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-header-001");

        ResponseEntity<ErrorResponse> response = handler.handleClientNotFound(new ClientNotFoundException("client-001"), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("CLIENT_NOT_FOUND", response.getBody().code());
        assertEquals("corr-header-001", response.getBody().correlationId());
    }

    @Test
    void shouldReturnValidationErrorForUnreadablePayload() {
        MockHttpServletRequest request = request("POST", "/subscriptions");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-payload-001");

        ResponseEntity<ErrorResponse> response = handler.handleUnreadableRequest(
                new HttpMessageNotReadableException("bad payload", new MockHttpInputMessage(new byte[0])),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("INVALID_REQUEST_PAYLOAD", response.getBody().code());
        assertEquals("Invalid request payload", response.getBody().message());
        assertEquals("corr-payload-001", response.getBody().correlationId());
    }

    @Test
    void shouldHandleControlledBusinessExceptionsWithExpectedCodes() {
        MockHttpServletRequest request = request("POST", "/subscriptions");

        assertEquals(
                "FUND_NOT_FOUND",
                handler.handleFundNotFound(new FundNotFoundException("fund-001"), request).getBody().code()
        );
        assertEquals(
                "INSUFFICIENT_BALANCE",
                handler.handleInsufficientBalance(new InsufficientBalanceException("fund-001"), request).getBody().code()
        );
        assertEquals(
                "ACTIVE_SUBSCRIPTION_ALREADY_EXISTS",
                handler.handleActiveSubscriptionAlreadyExists(
                        new ActiveSubscriptionAlreadyExistsException("client-001", "fund-001"),
                        request
                ).getBody().code()
        );
        assertEquals(
                "SUBSCRIPTION_NOT_FOUND",
                handler.handleSubscriptionNotFound(new SubscriptionNotFoundException("sub-001"), request).getBody().code()
        );
        assertEquals(
                "SUBSCRIPTION_ALREADY_CANCELLED",
                handler.handleSubscriptionAlreadyCancelled(new SubscriptionAlreadyCancelledException("sub-001"), request)
                        .getBody().code()
        );
        assertEquals(
                "MINIMUM_SUBSCRIPTION_AMOUNT",
                handler.handleMinimumSubscriptionAmount(new MinimumSubscriptionAmountException("fund-001"), request)
                        .getBody().code()
        );
    }

    @Test
    void shouldHandlePersistenceAndRetrievalExceptionsAsInternalServerError() {
        MockHttpServletRequest request = request("GET", "/transactions");

        assertEquals(
                "FUNDS_RETRIEVAL_ERROR",
                handler.handleFundsRetrieval(new FundsRetrievalException("Could not retrieve available funds", new RuntimeException("boom")), request)
                        .getBody().code()
        );
        assertEquals(
                "TRANSACTION_HISTORY_RETRIEVAL_ERROR",
                handler.handleTransactionHistoryRetrieval(new TransactionHistoryRetrievalException(new RuntimeException("boom")), request)
                        .getBody().code()
        );
        assertEquals(
                "SUBSCRIPTION_PERSISTENCE_ERROR",
                handler.handleSubscriptionPersistence(new SubscriptionPersistenceException(new RuntimeException("boom")), request)
                        .getBody().code()
        );
        assertEquals(
                "SUBSCRIPTION_CANCELLATION_PERSISTENCE_ERROR",
                handler.handleSubscriptionCancellationPersistence(
                        new SubscriptionCancellationPersistenceException(new RuntimeException("boom")),
                        request
                ).getBody().code()
        );
    }

    @Test
    void shouldHandleExplicitInvalidRequestException() {
        MockHttpServletRequest request = request("GET", "/transactions");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(
                new InvalidRequestException("clientId has an invalid format"),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("REQUEST_VALIDATION_ERROR", response.getBody().code());
        assertEquals("clientId has an invalid format", response.getBody().message());
    }

    @Test
    void shouldReturnValidationErrorsForInvalidBody() throws NoSuchMethodException {
        MockHttpServletRequest request = request("POST", "/subscriptions");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createSubscriptionRequest");
        bindingResult.addError(new FieldError("createSubscriptionRequest", "clientId", "clientId is required"));
        bindingResult.addError(new FieldError("createSubscriptionRequest", "amount", "amount must be greater than zero"));
        Method method = SampleController.class.getDeclaredMethod("sampleMethod", SamplePayload.class);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0),
                bindingResult
        );

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("REQUEST_VALIDATION_ERROR", response.getBody().code());
        assertEquals(
                "clientId: clientId is required, amount: amount must be greater than zero",
                response.getBody().message()
        );
        assertNotNull(response.getBody().correlationId());
    }

    @Test
    void shouldUseMdcCorrelationIdWhenHeaderIsMissing() throws Exception {
        MDC.put("correlationId", "corr-mdc-001");
        MockHttpServletRequest request = request("GET", "/transactions");

        ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameter(
                new MissingServletRequestParameterException("clientId", "String"),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("REQUEST_VALIDATION_ERROR", response.getBody().code());
        assertEquals("clientId is required", response.getBody().message());
        assertEquals("corr-mdc-001", response.getBody().correlationId());
    }

    @Test
    void shouldReturnGeneratedCorrelationIdForUnexpectedErrors() {
        MockHttpServletRequest request = request("GET", "/funds");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("Unexpected internal error", response.getBody().message());
        assertNotNull(response.getBody().correlationId());
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(path);
        return request;
    }

    @SuppressWarnings("unused")
    private static final class SampleController {
        public void sampleMethod(SamplePayload payload) {
        }
    }

    @SuppressWarnings("unused")
    private static final class SamplePayload {
    }
}
