package co.com.pactual.model.transaction;
import co.com.pactual.model.enums.TransactionType;
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
public class Transaction {
    private String transactionId;
    private String clientId;
    private String fundId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
