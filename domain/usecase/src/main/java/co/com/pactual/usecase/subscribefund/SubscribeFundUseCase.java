package co.com.pactual.usecase.subscribefund;

import co.com.pactual.model.client.Client;
import co.com.pactual.model.client.gateways.ClientRepository;
import co.com.pactual.model.enums.NotificationChannel;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.enums.TransactionType;
import co.com.pactual.model.fund.Fund;
import co.com.pactual.model.fund.gateways.FundRepository;
import co.com.pactual.model.gateways.NotificationGateway;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.model.subscription.gateways.SubscriptionRepository;
import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.model.transaction.gateways.TransactionRepository;
import co.com.pactual.usecase.subscribefund.exception.ActiveSubscriptionAlreadyExistsException;
import co.com.pactual.usecase.subscribefund.exception.ClientNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.FundNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.InsufficientBalanceException;
import co.com.pactual.usecase.subscribefund.exception.MinimumSubscriptionAmountException;
import co.com.pactual.usecase.subscribefund.exception.SubscriptionPersistenceException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class SubscribeFundUseCase {
    private static final BigDecimal DEFAULT_INITIAL_BALANCE = BigDecimal.valueOf(500_000L);

    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationGateway notificationGateway;

    public Subscription execute(String clientId, String fundId) {
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));
        return execute(clientId, fundId, fund.getMinimumAmount(), fund);
    }

    public Subscription execute(String clientId, String fundId, BigDecimal amount) {
        return execute(clientId, fundId, amount, null);
    }

    private Subscription execute(String clientId, String fundId, BigDecimal amount, Fund resolvedFund) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        Fund fund = resolvedFund != null ? resolvedFund : fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));

        validateNoActiveSubscription(clientId, fundId);
        validateMinimumAmount(amount, fund);
        validateAvailableBalance(client, amount, fund);

        try {
            LocalDateTime now = LocalDateTime.now();
            Subscription subscription = subscriptionRepository.save(buildSubscription(clientId, fundId, amount, now));

            Client updatedClient = updateClientBalance(client, amount);
            clientRepository.save(updatedClient);

            transactionRepository.save(buildTransaction(subscription.getSubscriptionId(), clientId, fundId, amount, now));
            notifyClient(updatedClient, fund, amount);

            return subscription;
        } catch (RuntimeException exception) {
            throw new SubscriptionPersistenceException(exception);
        }
    }

    private void validateNoActiveSubscription(String clientId, String fundId) {
        subscriptionRepository.findActiveByClientAndFund(clientId, fundId)
                .ifPresent(subscription -> {
                    throw new ActiveSubscriptionAlreadyExistsException(clientId, fundId);
                });
    }

    private void validateMinimumAmount(BigDecimal amount, Fund fund) {
        if (amount == null || amount.compareTo(fund.getMinimumAmount()) < 0) {
            throw new MinimumSubscriptionAmountException(fund.getName());
        }
    }

    private void validateAvailableBalance(Client client, BigDecimal amount, Fund fund) {
        if (resolveBalance(client).compareTo(amount) < 0) {
            throw new InsufficientBalanceException(fund.getName());
        }
    }

    private Subscription buildSubscription(String clientId, String fundId, BigDecimal amount, LocalDateTime now) {
        return Subscription.builder()
                .subscriptionId(UUID.randomUUID().toString())
                .clientId(clientId)
                .fundId(fundId)
                .amount(amount)
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(now)
                .cancelledAt(null)
                .build();
    }

    private Client updateClientBalance(Client client, BigDecimal amount) {
        return client.toBuilder()
                .availableBalance(resolveBalance(client).subtract(amount))
                .build();
    }

    private Transaction buildTransaction(String subscriptionId, String clientId, String fundId, BigDecimal amount, LocalDateTime now) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .subscriptionId(subscriptionId)
                .clientId(clientId)
                .fundId(fundId)
                .type(TransactionType.SUBSCRIPTION)
                .amount(amount)
                .createdAt(now)
                .build();
    }

    private void notifyClient(Client client, Fund fund, BigDecimal amount) {
        if (client.getNotificationPreference() == null) {
            return;
        }
        String message = buildNotificationMessage(client, fund, amount);
        NotificationChannel channel = client.getNotificationPreference();
        String destination = NotificationChannel.SMS.equals(channel) ? client.getPhone() : client.getEmail();
        try {
            notificationGateway.sendNotification(destination, message, channel);
        } catch (RuntimeException ignored) {
            // Notifications are best-effort and should not fail the main flow.
        }
    }

    private String buildNotificationMessage(Client client, Fund fund, BigDecimal amount) {
        return "Cliente " + client.getName()
                + ", su suscripcion al fondo "
                + fund.getName()
                + " por valor de "
                + amount.toPlainString()
                + " fue creada exitosamente.";
    }

    private BigDecimal resolveBalance(Client client) {
        return client.getAvailableBalance() != null ? client.getAvailableBalance() : DEFAULT_INITIAL_BALANCE;
    }
}
