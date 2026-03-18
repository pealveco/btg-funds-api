package co.com.pactual.api.subscription;

import co.com.pactual.api.subscription.dto.CreateSubscriptionRequest;
import co.com.pactual.api.subscription.dto.SubscriptionResponse;
import co.com.pactual.api.exception.InvalidRequestException;
import co.com.pactual.api.helper.InputSanitizer;
import co.com.pactual.api.subscription.mapper.SubscriptionMapper;
import co.com.pactual.usecase.cancelsubscription.CancelSubscriptionUseCase;
import co.com.pactual.usecase.subscribefund.SubscribeFundUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscribeFundUseCase subscribeFundUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        return SubscriptionMapper.toResponse(
                subscribeFundUseCase.execute(
                        request.getClientId(),
                        request.getFundId(),
                        request.getAmount()
                )
        );
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSubscription(
            @PathVariable("subscriptionId")
            @NotBlank(message = "subscriptionId is required")
            @Size(max = 100, message = "subscriptionId must not exceed 100 characters")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "subscriptionId has an invalid format")
            String subscriptionId
    ) {
        String normalizedSubscriptionId = InputSanitizer.trim(subscriptionId);
        if (normalizedSubscriptionId == null || normalizedSubscriptionId.isEmpty()) {
            throw new InvalidRequestException("subscriptionId is required");
        }
        if (!InputSanitizer.hasValidIdentifierFormat(normalizedSubscriptionId)) {
            throw new InvalidRequestException("subscriptionId has an invalid format");
        }

        cancelSubscriptionUseCase.execute(normalizedSubscriptionId);
    }
}
