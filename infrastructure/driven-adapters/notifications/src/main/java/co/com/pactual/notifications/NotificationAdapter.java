package co.com.pactual.notifications;

import co.com.pactual.model.enums.NotificationChannel;
import co.com.pactual.model.gateways.NotificationGateway;

public class NotificationAdapter implements NotificationGateway {

    @Override
    public void sendNotification(String clientId, String message, NotificationChannel channel) {
        if (channel == NotificationChannel.EMAIL) {
            // lógica mock email
        } else {
            // lógica mock SMS
        }
    }
}