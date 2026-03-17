package co.com.pactual.model.subscription.gateways;

import co.com.pactual.model.subscription.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(String subscriptionId);
    List<Subscription> findByClientId(String clientId);
    Optional<Subscription> findActiveByClientAndFund(String clientId, String fundId);
}
