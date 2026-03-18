package co.com.pactual.usecase.cancelsubscription.exception;

public class SubscriptionCancellationPersistenceException extends RuntimeException {

    public SubscriptionCancellationPersistenceException(Throwable cause) {
        super("Could not cancel subscription", cause);
    }
}
