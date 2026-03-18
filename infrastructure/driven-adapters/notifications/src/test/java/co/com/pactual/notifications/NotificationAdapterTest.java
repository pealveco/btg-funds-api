package co.com.pactual.notifications;

import co.com.pactual.model.enums.NotificationChannel;
import co.com.pactual.model.notification.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationAdapterTest {

    @Test
    void shouldPublishEventWhenTopicArnIsConfigured() {
        SnsClient snsClient = mock(SnsClient.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        NotificationAdapter adapter = new NotificationAdapter(
                snsClient,
                objectMapper,
                "arn:aws:sns:us-east-1:123456789012:btg-funds-api-dev-notifications"
        );

        adapter.publish(buildEvent("SUBSCRIPTION_CREATED"));

        verify(snsClient).publish(any(PublishRequest.class));
    }

    @Test
    void shouldSkipPublishWhenTopicArnIsBlank() {
        SnsClient snsClient = mock(SnsClient.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        NotificationAdapter adapter = new NotificationAdapter(snsClient, objectMapper, "");

        adapter.publish(buildEvent("SUBSCRIPTION_CREATED"));

        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    void shouldSkipPublishWhenTopicArnIsInvalid() {
        SnsClient snsClient = mock(SnsClient.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        NotificationAdapter adapter = new NotificationAdapter(snsClient, objectMapper, "invalid-arn");

        adapter.publish(buildEvent("SUBSCRIPTION_CREATED"));

        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    void shouldNotPropagateWhenSnsFails() {
        SnsClient snsClient = mock(SnsClient.class);
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("boom"));
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        NotificationAdapter adapter = new NotificationAdapter(
                snsClient,
                objectMapper,
                "arn:aws:sns:us-east-1:123456789012:btg-funds-api-dev-notifications"
        );

        adapter.publish(buildEvent("SUBSCRIPTION_CANCELLED"));

        verify(snsClient).publish(any(PublishRequest.class));
    }

    private NotificationEvent buildEvent(String eventType) {
        return NotificationEvent.builder()
                .eventType(eventType)
                .subscriptionId("sub-001")
                .clientId("client-001")
                .fundId("1")
                .amount(BigDecimal.valueOf(100_000L))
                .timestamp(LocalDateTime.of(2026, 3, 18, 2, 0))
                .status("ACTIVE")
                .channel(NotificationChannel.EMAIL)
                .destination("ana.perez@example.com")
                .build();
    }
}
