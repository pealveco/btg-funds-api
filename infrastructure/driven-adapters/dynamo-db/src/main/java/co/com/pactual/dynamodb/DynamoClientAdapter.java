package co.com.pactual.dynamodb;

import co.com.pactual.dynamodb.helper.TemplateAdapterOperations;
import co.com.pactual.model.client.Client;
import co.com.pactual.model.client.gateways.ClientRepository;
import co.com.pactual.model.enums.NotificationChannel;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.Optional;

@Repository
public class DynamoClientAdapter extends TemplateAdapterOperations<Client, String, ClientEntity> implements ClientRepository {

    public DynamoClientAdapter(
            DynamoDbEnhancedClient connectionFactory,
            ObjectMapper mapper,
            @Value("${adapters.dynamodb.tables.clients}") String tableName
    ) {
        super(connectionFactory, mapper, DynamoClientAdapter::toDomain, tableName);
    }

    @Override
    public Optional<Client> findById(String clientId) {
        return Optional.ofNullable(getById(clientId));
    }

    @Override
    protected ClientEntity toEntity(Client model) {
        ClientEntity entity = new ClientEntity();
        entity.setClientId(model.getClientId());
        entity.setName(model.getName());
        entity.setEmail(model.getEmail());
        entity.setPhone(model.getPhone());
        entity.setNotificationPreference(model.getNotificationPreference() != null ? model.getNotificationPreference().name() : null);
        entity.setAvailableBalance(model.getAvailableBalance());
        return entity;
    }

    private static Client toDomain(ClientEntity entity) {
        return Client.builder()
                .clientId(entity.getClientId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .notificationPreference(entity.getNotificationPreference() != null
                        ? NotificationChannel.valueOf(entity.getNotificationPreference())
                        : null)
                .availableBalance(entity.getAvailableBalance())
                .build();
    }
}
