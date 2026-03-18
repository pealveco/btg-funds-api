package co.com.pactual.usecase.subscribefund.exception;

public class SubscriptionPersistenceException extends RuntimeException {

    public SubscriptionPersistenceException(Throwable cause) {
        super("Could not create subscription", cause);
    }
}
