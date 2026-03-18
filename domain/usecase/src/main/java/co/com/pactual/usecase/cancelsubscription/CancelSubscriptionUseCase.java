package co.com.pactual.usecase.cancelsubscription;

import co.com.pactual.model.client.Client;
import co.com.pactual.model.client.gateways.ClientRepository;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.enums.TransactionType;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.model.subscription.gateways.SubscriptionRepository;
import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.model.transaction.gateways.TransactionRepository;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionAlreadyCancelledException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionCancellationPersistenceException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.ClientNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CancelSubscriptionUseCase {

    private static final java.math.BigDecimal DEFAULT_INITIAL_BALANCE = java.math.BigDecimal.valueOf(500_000L);

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;

    public void execute(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));

        if (SubscriptionStatus.CANCELLED.equals(subscription.getStatus())) {
            throw new SubscriptionAlreadyCancelledException(subscriptionId);
        }

        Client client = clientRepository.findById(subscription.getClientId())
                .orElseThrow(() -> new ClientNotFoundException(subscription.getClientId()));

        try {
            LocalDateTime now = LocalDateTime.now();
            Subscription cancelledSubscription = subscription.toBuilder()
                    .status(SubscriptionStatus.CANCELLED)
                    .cancelledAt(now)
                    .build();

            subscriptionRepository.save(cancelledSubscription);
            clientRepository.save(client.toBuilder()
                    .availableBalance(resolveBalance(client).add(subscription.getAmount()))
                    .build());
            transactionRepository.save(buildCancellationTransaction(subscription, now));
        } catch (RuntimeException exception) {
            throw new SubscriptionCancellationPersistenceException(exception);
        }
    }

    private Transaction buildCancellationTransaction(Subscription subscription, LocalDateTime now) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .subscriptionId(subscription.getSubscriptionId())
                .clientId(subscription.getClientId())
                .fundId(subscription.getFundId())
                .type(TransactionType.CANCELLATION)
                .amount(subscription.getAmount())
                .createdAt(now)
                .build();
    }

    private java.math.BigDecimal resolveBalance(Client client) {
        return client.getAvailableBalance() != null ? client.getAvailableBalance() : DEFAULT_INITIAL_BALANCE;
    }
}
