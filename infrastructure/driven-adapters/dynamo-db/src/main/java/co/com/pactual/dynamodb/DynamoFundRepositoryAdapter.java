package co.com.pactual.dynamodb;

import co.com.pactual.dynamodb.helper.TemplateAdapterOperations;
import co.com.pactual.model.fund.Fund;
import co.com.pactual.model.fund.gateways.FundRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.List;
import java.util.Optional;

@Repository
public class DynamoFundRepositoryAdapter extends TemplateAdapterOperations<Fund, String, FundEntity> implements FundRepository {

    public DynamoFundRepositoryAdapter(
            DynamoDbEnhancedClient connectionFactory,
            ObjectMapper mapper,
            @Value("${adapters.dynamodb.tables.funds}") String tableName
    ) {
        super(connectionFactory, mapper, DynamoFundRepositoryAdapter::toDomain, tableName);
    }

    @Override
    public Optional<Fund> findById(String fundId) {
        return Optional.ofNullable(getById(fundId));
    }

    @Override
    public List<Fund> findAll() {
        return scan();
    }

    @Override
    protected FundEntity toEntity(Fund model) {
        FundEntity entity = new FundEntity();
        entity.setFundId(model.getFundId());
        entity.setName(model.getName());
        entity.setMinimumAmount(model.getMinimumAmount());
        entity.setCategory(model.getCategory());
        return entity;
    }

    private static Fund toDomain(FundEntity entity) {
        return Fund.builder()
                .fundId(entity.getFundId())
                .name(entity.getName())
                .minimumAmount(entity.getMinimumAmount())
                .category(entity.getCategory())
                .build();
    }
}
