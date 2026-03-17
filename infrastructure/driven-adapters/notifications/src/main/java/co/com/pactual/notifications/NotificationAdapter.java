package co.com.pactual.notifications;

import co.com.pactual.model.enums.NotificationChannel;
import co.com.pactual.model.gateways.NotificationGateway;
import org.springframework.stereotype.Component;

@Component
public class NotificationAdapter implements NotificationGateway {

    @Override
    public void sendNotification(String clientId, String message, NotificationChannel channel) {
        if (channel == NotificationChannel.EMAIL) {
            // lógica mock email
            System.out.println("Enviando email a " + clientId);
        } else {
            // lógica mock SMS
            System.out.println("Enviando SMS a " + clientId);
        }
    }
}
