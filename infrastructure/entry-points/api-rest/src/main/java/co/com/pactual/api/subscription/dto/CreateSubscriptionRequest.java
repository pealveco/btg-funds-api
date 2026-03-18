package co.com.pactual.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CreateSubscriptionRequest {

    @NotBlank(message = "clientId is required")
    private String clientId;
    @NotBlank(message = "fundId is required")
    private String fundId;
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    public CreateSubscriptionRequest() {
    }

}
