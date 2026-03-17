package co.com.pactual.usecase.subscribefund.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String clientId) {
        super("Cliente no encontrado con id " + clientId);
    }
}
