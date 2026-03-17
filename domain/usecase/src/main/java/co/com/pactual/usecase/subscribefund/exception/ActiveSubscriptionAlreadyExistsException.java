package co.com.pactual.usecase.subscribefund.exception;

public class ActiveSubscriptionAlreadyExistsException extends RuntimeException {
    public ActiveSubscriptionAlreadyExistsException(String clientId, String fundId) {
        super("Ya existe una suscripcion activa para el cliente " + clientId + " en el fondo " + fundId);
    }
}
