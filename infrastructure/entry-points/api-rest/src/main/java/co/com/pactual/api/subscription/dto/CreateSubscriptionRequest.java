package co.com.pactual.api.subscription.dto;

import co.com.pactual.api.helper.InputSanitizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateSubscriptionRequest {

    @NotBlank(message = "clientId is required")
    @Size(max = 100, message = "clientId must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "clientId has an invalid format")
    private String clientId;

    @NotBlank(message = "fundId is required")
    @Size(max = 100, message = "fundId must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "fundId has an invalid format")
    private String fundId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    public CreateSubscriptionRequest() {
    }

    public void setClientId(String clientId) {
        this.clientId = InputSanitizer.trim(clientId);
    }

    public void setFundId(String fundId) {
        this.fundId = InputSanitizer.trim(fundId);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
