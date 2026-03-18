package co.com.pactual.api.subscription.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CreateSubscriptionRequest {

    private String clientId;
    private String fundId;
    private BigDecimal amount;

    public CreateSubscriptionRequest() {
    }

}
