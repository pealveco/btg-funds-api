package co.com.pactual.usecase.gettransactionhistory;

import co.com.pactual.model.transaction.Transaction;
import co.com.pactual.model.transaction.gateways.TransactionRepository;
import co.com.pactual.usecase.gettransactionhistory.exception.TransactionHistoryRetrievalException;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final TransactionRepository transactionRepository;

    public List<Transaction> execute(String clientId) {
        try {
            return transactionRepository.findByClientId(clientId).stream()
                    .sorted(Comparator.comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .toList();
        } catch (RuntimeException exception) {
            throw new TransactionHistoryRetrievalException(exception);
        }
    }
}
