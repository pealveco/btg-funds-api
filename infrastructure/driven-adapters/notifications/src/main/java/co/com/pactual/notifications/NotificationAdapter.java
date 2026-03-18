package co.com.pactual.notifications;

import co.com.pactual.model.gateways.NotificationGateway;
import co.com.pactual.model.notification.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class NotificationAdapter implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificationAdapter.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public NotificationAdapter(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sns.topic-arn:}") String topicArn
    ) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    @Override
    public void publish(NotificationEvent event) {
        if (topicArn == null || topicArn.isBlank()) {
            log.warn("SNS topic ARN is not configured. Skipping event publication for eventType={}", event.getEventType());
            return;
        }
        if (!topicArn.startsWith("arn:aws:sns:")) {
            log.warn("SNS topic ARN is invalid: {}. Skipping event publication for eventType={}", topicArn, event.getEventType());
            return;
        }

        try {
            snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(objectMapper.writeValueAsString(event))
                    .build());
            log.info("SNS event published successfully. eventType={} subscriptionId={}",
                    event.getEventType(), event.getSubscriptionId());
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize SNS event. eventType={} subscriptionId={}",
                    event.getEventType(), event.getSubscriptionId(), exception);
        } catch (RuntimeException exception) {
            log.error("Failed to publish SNS event. eventType={} subscriptionId={}",
                    event.getEventType(), event.getSubscriptionId(), exception);
        }
    }
}
