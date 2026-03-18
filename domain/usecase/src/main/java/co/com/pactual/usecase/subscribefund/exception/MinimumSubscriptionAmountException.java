package co.com.pactual.usecase.subscribefund.exception;

public class MinimumSubscriptionAmountException extends RuntimeException {

    public MinimumSubscriptionAmountException(String fundName) {
        super("El monto de la suscripcion no cumple el minimo del fondo " + fundName);
    }
}
