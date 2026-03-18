package co.com.pactual.model.notification;

import co.com.pactual.model.enums.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class NotificationEvent {

    private String eventType;
    private String subscriptionId;
    private String clientId;
    private String fundId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;
    private NotificationChannel channel;
    private String destination;
}
