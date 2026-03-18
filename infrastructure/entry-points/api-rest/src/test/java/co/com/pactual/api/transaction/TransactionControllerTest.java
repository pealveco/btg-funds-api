package co.com.pactual.api.transaction;

import co.com.pactual.api.exception.GlobalExceptionHandler;
import co.com.pactual.model.enums.TransactionType;
import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.usecase.gettransactionhistory.GetTransactionHistoryUseCase;
import co.com.pactual.usecase.gettransactionhistory.exception.TransactionHistoryRetrievalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    private TransactionController transactionController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        transactionController = new TransactionController(getTransactionHistoryUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnTransactions() throws Exception {
        when(getTransactionHistoryUseCase.execute("client-001")).thenReturn(List.of(
                transaction("tx-2", TransactionType.CANCELLATION, LocalDateTime.of(2026, 3, 18, 12, 0)),
                transaction("tx-1", TransactionType.SUBSCRIPTION, LocalDateTime.of(2026, 3, 18, 10, 0))
        ));

        mockMvc.perform(get("/transactions").param("clientId", "client-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].transactionId").value("tx-2"))
                .andExpect(jsonPath("$[0].type").value("CANCELLATION"))
                .andExpect(jsonPath("$[1].transactionId").value("tx-1"));
    }

    @Test
    void shouldTrimClientIdBeforeCallingUseCase() throws Exception {
        when(getTransactionHistoryUseCase.execute("client-001")).thenReturn(List.of());

        mockMvc.perform(get("/transactions").param("clientId", "  client-001  "))
                .andExpect(status().isOk());

        verify(getTransactionHistoryUseCase).execute("client-001");
    }

    @Test
    void shouldReturnBadRequestWhenClientIdIsMissing() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("clientId is required"));
    }

    @Test
    void shouldReturnBadRequestWhenClientIdIsBlank() throws Exception {
        mockMvc.perform(get("/transactions").param("clientId", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("clientId is required"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUseCaseFails() throws Exception {
        when(getTransactionHistoryUseCase.execute("client-001"))
                .thenThrow(new TransactionHistoryRetrievalException(new RuntimeException("boom")));

        mockMvc.perform(get("/transactions").param("clientId", "client-001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("TRANSACTION_HISTORY_RETRIEVAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Could not retrieve transaction history"));
    }

    @Test
    void shouldReturnBadRequestWhenClientIdFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/transactions").param("clientId", "client 001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("clientId has an invalid format"));
    }

    private Transaction transaction(String transactionId, TransactionType type, LocalDateTime createdAt) {
        return Transaction.builder()
                .transactionId(transactionId)
                .subscriptionId("sub-" + transactionId)
                .clientId("client-001")
                .fundId("1")
                .type(type)
                .amount(BigDecimal.valueOf(100_000L))
                .createdAt(createdAt)
                .build();
    }
}
