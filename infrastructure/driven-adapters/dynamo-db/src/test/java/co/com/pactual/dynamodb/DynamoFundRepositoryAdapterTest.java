package co.com.pactual.dynamodb;

import co.com.pactual.model.fund.Fund;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapperImp;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoFundRepositoryAdapterTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<FundEntity> table;

    @Mock
    private PageIterable<FundEntity> pageIterable;

    @Mock
    private SdkIterable<FundEntity> itemsIterable;

    private DynamoFundRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("funds-table"), any(TableSchema.class))).thenReturn(table);
        adapter = new DynamoFundRepositoryAdapter(enhancedClient, new ObjectMapperImp(), "funds-table");
    }

    @Test
    void shouldSaveFundMappingFieldsCorrectly() {
        Fund fund = Fund.builder()
                .fundId("fund-001")
                .name("Fondo Test")
                .minimumAmount(BigDecimal.valueOf(75_000L))
                .category("FPV")
                .build();

        Fund saved = adapter.save(fund);

        ArgumentCaptor<FundEntity> captor = ArgumentCaptor.forClass(FundEntity.class);
        verify(table).putItem(captor.capture());
        assertEquals("fund-001", captor.getValue().getFundId());
        assertEquals("Fondo Test", saved.getName());
    }

    @Test
    void shouldFindFundById() {
        FundEntity entity = new FundEntity();
        entity.setFundId("fund-001");
        entity.setName("Fondo Test");
        entity.setMinimumAmount(BigDecimal.valueOf(100_000L));
        entity.setCategory("FIC");
        when(table.getItem(any(Key.class))).thenReturn(entity);

        Optional<Fund> result = adapter.findById("fund-001");

        assertTrue(result.isPresent());
        assertEquals("FIC", result.get().getCategory());
    }

    @Test
    void shouldReturnAllFundsFromScan() {
        FundEntity first = new FundEntity();
        first.setFundId("1");
        first.setName("Fund 1");
        first.setMinimumAmount(BigDecimal.valueOf(50_000L));
        first.setCategory("FPV");

        FundEntity second = new FundEntity();
        second.setFundId("2");
        second.setName("Fund 2");
        second.setMinimumAmount(BigDecimal.valueOf(60_000L));
        second.setCategory("FIC");

        when(table.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(itemsIterable);
        when(itemsIterable.stream()).thenReturn(Stream.of(first, second));

        List<Fund> result = adapter.findAll();

        assertEquals(2, result.size());
        assertEquals("Fund 1", result.get(0).getName());
        assertEquals("FIC", result.get(1).getCategory());
    }
}
