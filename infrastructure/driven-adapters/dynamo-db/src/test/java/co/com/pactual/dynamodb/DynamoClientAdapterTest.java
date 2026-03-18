package co.com.pactual.dynamodb;

import co.com.pactual.model.client.Client;
import co.com.pactual.model.enums.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapperImp;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoClientAdapterTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<ClientEntity> table;

    private DynamoClientAdapter adapter;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("clients-table"), any(TableSchema.class))).thenReturn(table);
        adapter = new DynamoClientAdapter(enhancedClient, new ObjectMapperImp(), "clients-table");
    }

    @Test
    void shouldSaveClientMappingFieldsCorrectly() {
        Client client = Client.builder()
                .clientId("client-001")
                .name("Ana Perez")
                .email("ana@example.com")
                .phone("3001234567")
                .notificationPreference(NotificationChannel.EMAIL)
                .availableBalance(BigDecimal.valueOf(500_000L))
                .build();

        Client saved = adapter.save(client);

        ArgumentCaptor<ClientEntity> captor = ArgumentCaptor.forClass(ClientEntity.class);
        verify(table).putItem(captor.capture());

        ClientEntity entity = captor.getValue();
        assertEquals("client-001", entity.getClientId());
        assertEquals("Ana Perez", entity.getName());
        assertEquals("ana@example.com", entity.getEmail());
        assertEquals("3001234567", entity.getPhone());
        assertEquals("EMAIL", entity.getNotificationPreference());
        assertEquals(BigDecimal.valueOf(500_000L), entity.getAvailableBalance());
        assertEquals("client-001", saved.getClientId());
        assertEquals(NotificationChannel.EMAIL, saved.getNotificationPreference());
    }

    @Test
    void shouldFindClientByIdMappingEntityToDomain() {
        ClientEntity entity = new ClientEntity();
        entity.setClientId("client-001");
        entity.setName("Ana Perez");
        entity.setEmail("ana@example.com");
        entity.setPhone("3001234567");
        entity.setNotificationPreference("SMS");
        entity.setAvailableBalance(BigDecimal.valueOf(100_000L));
        when(table.getItem(any(Key.class))).thenReturn(entity);

        Optional<Client> result = adapter.findById("client-001");

        assertTrue(result.isPresent());
        assertEquals("client-001", result.get().getClientId());
        assertEquals(NotificationChannel.SMS, result.get().getNotificationPreference());
    }
}
