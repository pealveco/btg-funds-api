package co.com.pactual.usecase.gettransactionhistory.exception;

public class TransactionHistoryRetrievalException extends RuntimeException {

    public TransactionHistoryRetrievalException(Throwable cause) {
        super("Could not retrieve transaction history", cause);
    }
}
