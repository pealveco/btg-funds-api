package co.com.pactual.dynamodb;

import co.com.pactual.dynamodb.helper.TemplateAdapterOperations;
import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.model.transaction.gateways.TransactionRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;

@Repository
public class DynamoTransactionRepositoryAdapter extends TemplateAdapterOperations<Transaction, String, TransactionEntity>
        implements TransactionRepository {

    public DynamoTransactionRepositoryAdapter(
            DynamoDbEnhancedClient connectionFactory,
            ObjectMapper mapper,
            @Value("${adapters.dynamodb.tables.transactions}") String tableName
    ) {
        super(connectionFactory, mapper, DynamoTransactionRepositoryAdapter::toDomain, tableName, "clientId-createdAt-index");
    }

    @Override
    public List<Transaction> findByClientId(String clientId) {
        QueryEnhancedRequest query = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(clientId).build()))
                .build();
        return queryByIndex(query, "clientId-createdAt-index");
    }

    @Override
    protected TransactionEntity toEntity(Transaction model) {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(model.getTransactionId());
        entity.setClientId(model.getClientId());
        entity.setFundId(model.getFundId());
        entity.setType(model.getType() != null ? model.getType().name() : null);
        entity.setAmount(model.getAmount());
        entity.setCreatedAt(model.getCreatedAt());
        return entity;
    }

    private static Transaction toDomain(TransactionEntity entity) {
        return Transaction.builder()
                .transactionId(entity.getTransactionId())
                .clientId(entity.getClientId())
                .fundId(entity.getFundId())
                .type(entity.getType() != null ? co.com.pactual.model.enums.TransactionType.valueOf(entity.getType()) : null)
                .amount(entity.getAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
