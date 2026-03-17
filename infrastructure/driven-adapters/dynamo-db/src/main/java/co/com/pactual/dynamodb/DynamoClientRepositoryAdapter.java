package co.com.pactual.dynamodb;

import co.com.pactual.dynamodb.helper.TemplateAdapterOperations;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.model.subscription.gateways.SubscriptionRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;

@Repository
public class DynamoClientRepositoryAdapter extends TemplateAdapterOperations<Subscription, String, SubscriptionEntity> implements SubscriptionRepository {

    public DynamoClientRepositoryAdapter(
            DynamoDbEnhancedClient connectionFactory,
            ObjectMapper mapper,
            @Value("${adapters.dynamodb.tables.subscriptions}") String tableName
    ) {
        super(connectionFactory, mapper, DynamoClientRepositoryAdapter::toDomain, tableName, "clientId-index");
    }

    public List<Subscription> getEntityBySomeKeys(String partitionKey, String sortKey) {
        QueryEnhancedRequest queryExpression = generateQueryExpression(partitionKey, sortKey);
        return query(queryExpression);
    }

    public List<Subscription> getEntityBySomeKeysByIndex(String partitionKey, String sortKey) {
        QueryEnhancedRequest queryExpression = generateQueryExpression(partitionKey, sortKey);
        return queryByIndex(queryExpression);
    }

    private QueryEnhancedRequest generateQueryExpression(String partitionKey, String sortKey) {
        return QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(partitionKey).build()))
                .queryConditional(QueryConditional.sortGreaterThanOrEqualTo(Key.builder().sortValue(sortKey).build()))
                .build();
    }

    @Override
    public Optional<Subscription> findById(String subscriptionId) {
        return Optional.ofNullable(getById(subscriptionId));
    }

    @Override
    public List<Subscription> findByClientId(String clientId) {
        QueryEnhancedRequest query = QueryEnhancedRequest.builder()
                .queryConditional(
                        QueryConditional.keyEqualTo(
                                Key.builder().partitionValue(clientId).build()
                        )
                )
                .build();

        return queryByIndex(query, "clientId-index");
    }

    @Override
    public Optional<Subscription> findActiveByClientAndFund(String clientId, String fundId) {
        return findByClientId(clientId)
                .stream()
                .filter(sub -> fundId.equals(sub.getFundId()))
                .filter(sub -> sub.getStatus().equals(SubscriptionStatus.ACTIVE))
                .findFirst();
    }

    @Override
    protected SubscriptionEntity toEntity(Subscription model) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setSubscriptionId(model.getSubscriptionId());
        entity.setClientId(model.getClientId());
        entity.setFundId(model.getFundId());
        entity.setAmount(model.getAmount());
        entity.setStatus(model.getStatus() != null ? model.getStatus().name() : null);
        entity.setCreatedAt(model.getCreatedAt());
        entity.setCancelledAt(model.getCancelledAt());
        return entity;
    }

    private static Subscription toDomain(SubscriptionEntity entity) {
        return Subscription.builder()
                .subscriptionId(entity.getSubscriptionId())
                .clientId(entity.getClientId())
                .fundId(entity.getFundId())
                .amount(entity.getAmount())
                .status(entity.getStatus() != null ? SubscriptionStatus.valueOf(entity.getStatus()) : null)
                .createdAt(entity.getCreatedAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }
}
