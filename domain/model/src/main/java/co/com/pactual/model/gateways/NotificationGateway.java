package co.com.pactual.model.gateways;

import co.com.pactual.model.enums.NotificationChannel;

public interface NotificationGateway {
    void sendNotification(String destination, String message, NotificationChannel channel);
}