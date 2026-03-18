package co.com.pactual.api.subscription.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class SubscriptionResponse {

    private String subscriptionId;
    private String clientId;
    private String fundId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public SubscriptionResponse() {
    }

}
