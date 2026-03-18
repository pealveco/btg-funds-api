package co.com.pactual.api.subscription;

import co.com.pactual.api.subscription.dto.CreateSubscriptionRequest;
import co.com.pactual.api.subscription.dto.SubscriptionResponse;
import co.com.pactual.api.subscription.mapper.SubscriptionMapper;
import co.com.pactual.usecase.subscribefund.SubscribeFundUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscribeFundUseCase subscribeFundUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse createSubscription(@RequestBody CreateSubscriptionRequest request) {
        return SubscriptionMapper.toResponse(
                subscribeFundUseCase.execute(
                        request.getClientId(),
                        request.getFundId(),
                        request.getAmount()
                )
        );
    }
}
