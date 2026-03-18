package co.com.pactual.model.gateways;

import co.com.pactual.model.notification.NotificationEvent;

public interface NotificationGateway {
    void publish(NotificationEvent event);
}