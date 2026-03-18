package co.com.pactual.usecase.gettransactionhistory;

import co.com.pactual.model.enums.TransactionType;
import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.model.transaction.gateways.TransactionRepository;
import co.com.pactual.usecase.gettransactionhistory.exception.TransactionHistoryRetrievalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionHistoryUseCase useCase;

    @Test
    void shouldReturnTransactionsOrderedByMostRecentFirst() {
        when(transactionRepository.findByClientId("client-001")).thenReturn(List.of(
                transaction("tx-1", LocalDateTime.of(2026, 3, 18, 10, 0)),
                transaction("tx-3", LocalDateTime.of(2026, 3, 18, 12, 0)),
                transaction("tx-2", LocalDateTime.of(2026, 3, 18, 11, 0))
        ));

        List<Transaction> transactions = useCase.execute("client-001");

        assertEquals(List.of("tx-3", "tx-2", "tx-1"),
                transactions.stream().map(Transaction::getTransactionId).toList());
    }

    @Test
    void shouldWrapRepositoryFailures() {
        when(transactionRepository.findByClientId("client-001"))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(TransactionHistoryRetrievalException.class, () -> useCase.execute("client-001"));
    }

    private Transaction transaction(String transactionId, LocalDateTime createdAt) {
        return Transaction.builder()
                .transactionId(transactionId)
                .subscriptionId("sub-" + transactionId)
                .clientId("client-001")
                .fundId("1")
                .type(TransactionType.SUBSCRIPTION)
                .amount(BigDecimal.valueOf(100_000L))
                .createdAt(createdAt)
                .build();
    }
}
