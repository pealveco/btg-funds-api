package co.com.pactual.api.subscription.mapper;

import co.com.pactual.api.subscription.dto.SubscriptionResponse;
import co.com.pactual.model.subscription.Subscription;

public final class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    public static SubscriptionResponse toResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getSubscriptionId());
        response.setClientId(subscription.getClientId());
        response.setFundId(subscription.getFundId());
        response.setAmount(subscription.getAmount());
        response.setStatus(subscription.getStatus() != null ? subscription.getStatus().name() : null);
        response.setCreatedAt(subscription.getCreatedAt());
        return response;
    }
}
