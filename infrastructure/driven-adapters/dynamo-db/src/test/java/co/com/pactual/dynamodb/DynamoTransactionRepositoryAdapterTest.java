package co.com.pactual.dynamodb;

import co.com.pactual.model.enums.TransactionType;
import co.com.pactual.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapperImp;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoTransactionRepositoryAdapterTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<TransactionEntity> table;

    @Mock
    private DynamoDbIndex<TransactionEntity> index;

    @Mock
    private SdkIterable<Page<TransactionEntity>> sdkIterable;

    @Mock
    private Page<TransactionEntity> firstPage;

    @Mock
    private Page<TransactionEntity> secondPage;

    private DynamoTransactionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("transactions-table"), any(TableSchema.class))).thenReturn(table);
        when(table.index("clientId-createdAt-index")).thenReturn(index);
        adapter = new DynamoTransactionRepositoryAdapter(enhancedClient, new ObjectMapperImp(), "transactions-table");
    }

    @Test
    void shouldSaveTransactionMappingFieldsCorrectly() {
        Transaction transaction = Transaction.builder()
                .transactionId("tx-001")
                .subscriptionId("sub-001")
                .clientId("client-001")
                .fundId("fund-001")
                .type(TransactionType.SUBSCRIPTION)
                .amount(BigDecimal.valueOf(100_000L))
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = adapter.save(transaction);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(table).putItem(captor.capture());
        assertEquals("SUBSCRIPTION", captor.getValue().getType());
        assertEquals(TransactionType.SUBSCRIPTION, saved.getType());
    }

    @Test
    void shouldFindTransactionsByClientIdUsingConfiguredIndex() {
        TransactionEntity first = new TransactionEntity();
        first.setTransactionId("tx-001");
        first.setSubscriptionId("sub-001");
        first.setClientId("client-001");
        first.setFundId("fund-001");
        first.setType("SUBSCRIPTION");
        first.setAmount(BigDecimal.valueOf(100_000L));
        first.setCreatedAt(LocalDateTime.of(2026, 3, 18, 10, 0));

        TransactionEntity second = new TransactionEntity();
        second.setTransactionId("tx-002");
        second.setSubscriptionId("sub-001");
        second.setClientId("client-001");
        second.setFundId("fund-001");
        second.setType("CANCELLATION");
        second.setAmount(BigDecimal.valueOf(100_000L));
        second.setCreatedAt(LocalDateTime.of(2026, 3, 18, 12, 0));

        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(Stream.of(firstPage, secondPage));
        when(firstPage.items()).thenReturn(List.of(first));
        when(secondPage.items()).thenReturn(List.of(second));

        List<Transaction> result = adapter.findByClientId("client-001");

        assertEquals(2, result.size());
        assertEquals(TransactionType.SUBSCRIPTION, result.get(0).getType());
        assertEquals(TransactionType.CANCELLATION, result.get(1).getType());
    }
}
