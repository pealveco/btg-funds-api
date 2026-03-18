package co.com.pactual.api.transaction;

import co.com.pactual.api.exception.InvalidRequestException;
import co.com.pactual.api.transaction.dto.TransactionResponse;
import co.com.pactual.api.transaction.mapper.TransactionMapper;
import co.com.pactual.usecase.gettransactionhistory.GetTransactionHistoryUseCase;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TransactionController {

    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @GetMapping
    public List<TransactionResponse> getTransactions(@RequestParam("clientId") @NotBlank(message = "clientId is required") String clientId) {
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        if (normalizedClientId == null || normalizedClientId.isEmpty()) {
            throw new InvalidRequestException("clientId is required");
        }

        return getTransactionHistoryUseCase.execute(normalizedClientId).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }
}
