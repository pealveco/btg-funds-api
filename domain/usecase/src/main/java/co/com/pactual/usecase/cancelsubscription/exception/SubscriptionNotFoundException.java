package co.com.pactual.usecase.cancelsubscription.exception;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(String subscriptionId) {
        super("Suscripcion no encontrada con id " + subscriptionId);
    }
}
