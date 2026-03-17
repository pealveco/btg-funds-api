package co.com.pactual.model.subscription;
import co.com.pactual.model.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Subscription {
    private String subscriptionId;
    private String clientId;
    private String fundId;
    private BigDecimal amount;
    private SubscriptionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
}
