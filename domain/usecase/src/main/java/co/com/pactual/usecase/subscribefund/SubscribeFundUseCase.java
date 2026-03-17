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
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));

        validateNoActiveSubscription(clientId, fundId);
        validateAvailableBalance(client, fund);

        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = fund.getMinimumAmount();

        Subscription subscription = subscriptionRepository.save(buildSubscription(clientId, fundId, amount, now));

        Client updatedClient = updateClientBalance(client, amount);
        clientRepository.save(updatedClient);

        transactionRepository.save(buildTransaction(clientId, fundId, amount, now));
        notifyClient(updatedClient, fund, amount);

        return subscription;
    }

    private void validateNoActiveSubscription(String clientId, String fundId) {
        subscriptionRepository.findActiveByClientAndFund(clientId, fundId)
                .ifPresent(subscription -> {
                    throw new ActiveSubscriptionAlreadyExistsException(clientId, fundId);
                });
    }

    private void validateAvailableBalance(Client client, Fund fund) {
        if (resolveBalance(client).compareTo(fund.getMinimumAmount()) < 0) {
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

    private Transaction buildTransaction(String clientId, String fundId, BigDecimal amount, LocalDateTime now) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .clientId(clientId)
                .fundId(fundId)
                .type(TransactionType.SUBSCRIPTION)
                .amount(amount)
                .createdAt(now)
                .build();
    }

    private void notifyClient(Client client, Fund fund, BigDecimal amount) {
        String message = buildNotificationMessage(client, fund, amount);
        NotificationChannel channel = client.getNotificationPreference();
        String destination = NotificationChannel.SMS.equals(channel) ? client.getPhone() : client.getEmail();
        notificationGateway.sendNotification(destination, message, channel);
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
