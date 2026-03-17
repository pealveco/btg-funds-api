package co.com.pactual.model.fund.gateways;

import co.com.pactual.model.fund.Fund;

import java.util.List;
import java.util.Optional;

public interface FundRepository {
    Optional<Fund> findById(String fundId);
    List<Fund> findAll();
}
