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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeFundUseCaseTest {

    private static final String CLIENT_ID = "client-001";
    private static final String FUND_ID = "fund-001";
    private static final BigDecimal SUBSCRIPTION_AMOUNT = BigDecimal.valueOf(100_000L);

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private FundRepository fundRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private NotificationGateway notificationGateway;

    @InjectMocks
    private SubscribeFundUseCase useCase;

    private Client emailClient;
    private Client smsClient;
    private Fund fund;

    @BeforeEach
    void setUp() {
        emailClient = Client.builder()
                .clientId(CLIENT_ID)
                .name("Ana Perez")
                .email("ana@test.com")
                .phone("3001234567")
                .notificationPreference(NotificationChannel.EMAIL)
                .availableBalance(BigDecimal.valueOf(500_000L))
                .build();

        smsClient = emailClient.toBuilder()
                .notificationPreference(NotificationChannel.SMS)
                .build();

        fund = Fund.builder()
                .fundId(FUND_ID)
                .name("FPV_EL_CLIENTE_RECAUDADORA")
                .minimumAmount(BigDecimal.valueOf(100_000L))
                .category("FPV")
                .build();
    }

    @Test
    void shouldSubscribeSuccessfully() {
        mockCommonDependencies(emailClient, fund);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepository.save(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription subscription = useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT);

        assertNotNull(subscription.getSubscriptionId());
        assertEquals(CLIENT_ID, subscription.getClientId());
        assertEquals(FUND_ID, subscription.getFundId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(SUBSCRIPTION_AMOUNT, subscription.getAmount());
        assertNotNull(subscription.getCreatedAt());

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        assertEquals(BigDecimal.valueOf(400_000L), clientCaptor.getValue().getAvailableBalance());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertNotNull(transactionCaptor.getValue().getTransactionId());
        assertEquals(TransactionType.SUBSCRIPTION, transactionCaptor.getValue().getType());
        assertEquals(SUBSCRIPTION_AMOUNT, transactionCaptor.getValue().getAmount());
    }

    @Test
    void shouldThrowWhenFundNotFound() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(emailClient));
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.empty());

        assertThrows(FundNotFoundException.class, () -> useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT));

        verify(subscriptionRepository, never()).findActiveByClientAndFund(any(), any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenClientNotFound() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT));

        verify(fundRepository, never()).findById(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        Client clientWithoutBalance = emailClient.toBuilder()
                .availableBalance(BigDecimal.valueOf(50_000L))
                .build();
        mockCommonDependencies(clientWithoutBalance, fund);

        InsufficientBalanceException exception =
                assertThrows(InsufficientBalanceException.class, () -> useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT));

        assertEquals(
                "No tiene saldo disponible para vincularse al fondo " + fund.getName(),
                exception.getMessage()
        );
        verify(subscriptionRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAmountIsBelowFundMinimum() {
        mockCommonDependencies(emailClient, fund);

        MinimumSubscriptionAmountException exception = assertThrows(
                MinimumSubscriptionAmountException.class,
                () -> useCase.execute(CLIENT_ID, FUND_ID, BigDecimal.valueOf(90_000L))
        );

        assertEquals(
                "El monto de la suscripcion no cumple el minimo del fondo " + fund.getName(),
                exception.getMessage()
        );
        verify(subscriptionRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenActiveSubscriptionAlreadyExists() {
        mockCommonDependencies(emailClient, fund);
        when(subscriptionRepository.findActiveByClientAndFund(CLIENT_ID, FUND_ID))
                .thenReturn(Optional.of(existingSubscription()));

        assertThrows(ActiveSubscriptionAlreadyExistsException.class,
                () -> useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT));

        verify(subscriptionRepository, never()).save(any());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void shouldSendNotificationByEmail() {
        mockSuccessfulPersistence(emailClient, fund);

        useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT);

        verify(notificationGateway).sendNotification(
                eq(emailClient.getEmail()),
                any(String.class),
                eq(NotificationChannel.EMAIL)
        );
    }

    @Test
    void shouldSendNotificationBySms() {
        mockSuccessfulPersistence(smsClient, fund);

        useCase.execute(CLIENT_ID, FUND_ID, SUBSCRIPTION_AMOUNT);

        verify(notificationGateway).sendNotification(
                eq(smsClient.getPhone()),
                any(String.class),
                eq(NotificationChannel.SMS)
        );
    }

    private void mockCommonDependencies(Client client, Fund fundToUse) {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fundToUse));
        when(subscriptionRepository.findActiveByClientAndFund(CLIENT_ID, FUND_ID)).thenReturn(Optional.empty());
    }

    private void mockSuccessfulPersistence(Client client, Fund fundToUse) {
        mockCommonDependencies(client, fundToUse);
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepository.save(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Subscription existingSubscription() {
        return Subscription.builder()
                .subscriptionId("sub-001")
                .clientId(CLIENT_ID)
                .fundId(FUND_ID)
                .amount(SUBSCRIPTION_AMOUNT)
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }
}
