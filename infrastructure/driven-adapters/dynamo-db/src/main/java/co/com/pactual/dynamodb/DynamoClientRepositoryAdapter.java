package co.com.pactual.dynamodb;

import co.com.pactual.dynamodb.helper.TemplateAdapterOperations;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.model.subscription.gateways.SubscriptionRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;

@Repository
public class DynamoClientRepositoryAdapter extends TemplateAdapterOperations<Subscription, String, SubscriptionEntity> implements SubscriptionRepository {

    public DynamoClientRepositoryAdapter(DynamoDbEnhancedClient connectionFactory, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(connectionFactory, mapper, d -> mapper.map(d, Subscription.class), "subscriptions");
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
        return Optional.empty();
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

        return query(query);
    }

    @Override
    public Optional<Subscription> findActiveByClientAndFund(String clientId, String fundId) {
        QueryEnhancedRequest query = QueryEnhancedRequest.builder()
                .queryConditional(
                        QueryConditional.keyEqualTo(
                                Key.builder()
                                        .partitionValue(clientId)
                                        .sortValue(fundId)
                                        .build()
                        )
                )
                .build();

        return queryByIndex(query, "gsi_client_fund")
                .stream()
                .filter(sub -> sub.getStatus().equals(SubscriptionStatus.ACTIVE))
                .findFirst();
    }
}
