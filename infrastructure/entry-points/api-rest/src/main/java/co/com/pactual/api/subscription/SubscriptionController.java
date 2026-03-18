package co.com.pactual.api.subscription;

import co.com.pactual.api.subscription.dto.CreateSubscriptionRequest;
import co.com.pactual.api.subscription.dto.SubscriptionResponse;
import co.com.pactual.api.subscription.mapper.SubscriptionMapper;
import co.com.pactual.usecase.cancelsubscription.CancelSubscriptionUseCase;
import co.com.pactual.usecase.subscribefund.SubscribeFundUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public void cancelSubscription(@PathVariable("subscriptionId") String subscriptionId) {
        cancelSubscriptionUseCase.execute(subscriptionId);
    }
}
