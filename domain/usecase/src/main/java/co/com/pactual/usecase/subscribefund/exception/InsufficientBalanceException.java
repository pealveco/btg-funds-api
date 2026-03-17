package co.com.pactual.usecase.subscribefund.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String fundName) {
        super("No tiene saldo disponible para vincularse al fondo " + fundName);
    }
}
