package co.com.pactual.model.transaction.gateways;

import co.com.pactual.model.transaction.Transaction;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByClientId(String clientId);
}
