package co.com.pactual.api.transaction.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private String transactionId;
    private String subscriptionId;
    private String clientId;
    private String fundId;
    private String type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
