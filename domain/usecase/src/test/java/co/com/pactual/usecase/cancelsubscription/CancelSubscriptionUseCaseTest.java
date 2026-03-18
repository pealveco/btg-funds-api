package co.com.pactual.usecase.cancelsubscription;

import co.com.pactual.model.client.Client;
import co.com.pactual.model.client.gateways.ClientRepository;
import co.com.pactual.model.enums.NotificationChannel;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.enums.TransactionType;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.model.subscription.gateways.SubscriptionRepository;
import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.model.transaction.gateways.TransactionRepository;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionAlreadyCancelledException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelSubscriptionUseCaseTest {

    private static final String SUBSCRIPTION_ID = "sub-001";
    private static final String CLIENT_ID = "client-001";
    private static final String FUND_ID = "1";

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CancelSubscriptionUseCase useCase;

    private Subscription activeSubscription;
    private Client client;

    @BeforeEach
    void setUp() {
        activeSubscription = Subscription.builder()
                .subscriptionId(SUBSCRIPTION_ID)
                .clientId(CLIENT_ID)
                .fundId(FUND_ID)
                .amount(BigDecimal.valueOf(100_000L))
                .status(SubscriptionStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 3, 18, 0, 0))
                .build();

        client = Client.builder()
                .clientId(CLIENT_ID)
                .name("Ana Perez")
                .email("ana@test.com")
                .phone("3001234567")
                .notificationPreference(NotificationChannel.EMAIL)
                .availableBalance(BigDecimal.valueOf(400_000L))
                .build();
    }

    @Test
    void shouldCancelSubscriptionSuccessfully() {
        when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(activeSubscription));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(SUBSCRIPTION_ID);

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subscriptionCaptor.capture());
        assertEquals(SubscriptionStatus.CANCELLED, subscriptionCaptor.getValue().getStatus());
        assertEquals(activeSubscription.getAmount(), subscriptionCaptor.getValue().getAmount());
        assertEquals(activeSubscription.getCreatedAt(), subscriptionCaptor.getValue().getCreatedAt());
        assertEquals(SUBSCRIPTION_ID, subscriptionCaptor.getValue().getSubscriptionId());
        assertEquals(activeSubscription.getFundId(), subscriptionCaptor.getValue().getFundId());
        assertEquals(activeSubscription.getClientId(), subscriptionCaptor.getValue().getClientId());
        assertEquals(true, subscriptionCaptor.getValue().getCancelledAt() != null);

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        assertEquals(BigDecimal.valueOf(500_000L), clientCaptor.getValue().getAvailableBalance());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(TransactionType.CANCELLATION, transactionCaptor.getValue().getType());
        assertEquals(SUBSCRIPTION_ID, transactionCaptor.getValue().getSubscriptionId());
        assertEquals(CLIENT_ID, transactionCaptor.getValue().getClientId());
        assertEquals(FUND_ID, transactionCaptor.getValue().getFundId());
        assertEquals(activeSubscription.getAmount(), transactionCaptor.getValue().getAmount());
    }

    @Test
    void shouldThrowWhenSubscriptionDoesNotExist() {
        when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.empty());

        assertThrows(SubscriptionNotFoundException.class, () -> useCase.execute(SUBSCRIPTION_ID));

        verify(subscriptionRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSubscriptionIsAlreadyCancelled() {
        when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(
                activeSubscription.toBuilder()
                        .status(SubscriptionStatus.CANCELLED)
                        .cancelledAt(LocalDateTime.now())
                        .build()
        ));

        assertThrows(SubscriptionAlreadyCancelledException.class, () -> useCase.execute(SUBSCRIPTION_ID));

        verify(subscriptionRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
