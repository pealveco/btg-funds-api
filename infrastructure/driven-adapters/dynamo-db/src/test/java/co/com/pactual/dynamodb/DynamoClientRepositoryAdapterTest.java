package co.com.pactual.dynamodb;

import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.subscription.Subscription;
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
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoClientRepositoryAdapterTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<SubscriptionEntity> table;

    @Mock
    private DynamoDbIndex<SubscriptionEntity> index;

    @Mock
    private SdkIterable<Page<SubscriptionEntity>> sdkIterable;

    @Mock
    private Page<SubscriptionEntity> page;

    @Mock
    private PageIterable<SubscriptionEntity> pageIterable;

    private DynamoClientRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("subscriptions-table"), any(TableSchema.class))).thenReturn(table);
        when(table.index("clientId-index")).thenReturn(index);
        adapter = new DynamoClientRepositoryAdapter(enhancedClient, new ObjectMapperImp(), "subscriptions-table");
    }

    @Test
    void shouldSaveSubscriptionMappingFieldsCorrectly() {
        Subscription subscription = Subscription.builder()
                .subscriptionId("sub-001")
                .clientId("client-001")
                .fundId("fund-001")
                .amount(BigDecimal.valueOf(100_000L))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Subscription saved = adapter.save(subscription);

        ArgumentCaptor<SubscriptionEntity> captor = ArgumentCaptor.forClass(SubscriptionEntity.class);
        verify(table).putItem(captor.capture());
        assertEquals("sub-001", captor.getValue().getSubscriptionId());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals(SubscriptionStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void shouldFindSubscriptionById() {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setSubscriptionId("sub-001");
        entity.setClientId("client-001");
        entity.setFundId("fund-001");
        entity.setAmount(BigDecimal.valueOf(100_000L));
        entity.setStatus("CANCELLED");
        when(table.getItem(any(Key.class))).thenReturn(entity);

        Optional<Subscription> result = adapter.findById("sub-001");

        assertTrue(result.isPresent());
        assertEquals(SubscriptionStatus.CANCELLED, result.get().getStatus());
    }

    @Test
    void shouldFindSubscriptionsByClientIdUsingIndex() {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setSubscriptionId("sub-001");
        entity.setClientId("client-001");
        entity.setFundId("fund-001");
        entity.setAmount(BigDecimal.valueOf(100_000L));
        entity.setStatus("ACTIVE");

        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of(entity));

        List<Subscription> result = adapter.findByClientId("client-001");

        assertEquals(1, result.size());
        assertEquals("fund-001", result.get(0).getFundId());
    }

    @Test
    void shouldFindActiveSubscriptionByClientAndFund() {
        SubscriptionEntity active = new SubscriptionEntity();
        active.setSubscriptionId("sub-001");
        active.setClientId("client-001");
        active.setFundId("fund-001");
        active.setAmount(BigDecimal.valueOf(100_000L));
        active.setStatus("ACTIVE");

        SubscriptionEntity cancelled = new SubscriptionEntity();
        cancelled.setSubscriptionId("sub-002");
        cancelled.setClientId("client-001");
        cancelled.setFundId("fund-001");
        cancelled.setAmount(BigDecimal.valueOf(100_000L));
        cancelled.setStatus("CANCELLED");

        when(index.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(Stream.of(page));
        when(page.items()).thenReturn(List.of(cancelled, active));

        Optional<Subscription> result = adapter.findActiveByClientAndFund("client-001", "fund-001");

        assertTrue(result.isPresent());
        assertEquals("sub-001", result.get().getSubscriptionId());
    }

    @Test
    void shouldFailFastWhenQueryHelperBuildsInvalidConditional() {
        assertThrows(IllegalArgumentException.class,
                () -> adapter.getEntityBySomeKeys("client-001", "fund-001"));
        assertThrows(IllegalArgumentException.class,
                () -> adapter.getEntityBySomeKeysByIndex("client-001", "fund-001"));
    }
}
