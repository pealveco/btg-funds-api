package co.com.pactual.api.transaction.mapper;

import co.com.pactual.api.transaction.dto.TransactionResponse;
import co.com.pactual.model.transaction.Transaction;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setSubscriptionId(transaction.getSubscriptionId());
        response.setClientId(transaction.getClientId());
        response.setFundId(transaction.getFundId());
        response.setType(transaction.getType() != null ? transaction.getType().name() : null);
        response.setAmount(transaction.getAmount());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
