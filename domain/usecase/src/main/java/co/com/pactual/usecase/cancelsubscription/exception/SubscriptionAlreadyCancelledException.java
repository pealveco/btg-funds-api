package co.com.pactual.usecase.cancelsubscription.exception;

public class SubscriptionAlreadyCancelledException extends RuntimeException {

    public SubscriptionAlreadyCancelledException(String subscriptionId) {
        super("La suscripcion " + subscriptionId + " ya se encuentra cancelada");
    }
}
