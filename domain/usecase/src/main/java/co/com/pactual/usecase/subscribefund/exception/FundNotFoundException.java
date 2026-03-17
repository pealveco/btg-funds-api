package co.com.pactual.usecase.subscribefund.exception;

public class FundNotFoundException extends RuntimeException {
    public FundNotFoundException(String fundId) {
        super("Fondo no encontrado con id " + fundId);
    }
}
