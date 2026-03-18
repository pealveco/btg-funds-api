package co.com.pactual.api.transaction;

import co.com.pactual.api.exception.InvalidRequestException;
import co.com.pactual.api.helper.InputSanitizer;
import co.com.pactual.api.transaction.dto.TransactionResponse;
import co.com.pactual.api.transaction.mapper.TransactionMapper;
import co.com.pactual.usecase.gettransactionhistory.GetTransactionHistoryUseCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    public List<TransactionResponse> getTransactions(
            @RequestParam("clientId")
            @NotBlank(message = "clientId is required")
            @Size(max = 100, message = "clientId must not exceed 100 characters")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "clientId has an invalid format")
            String clientId
    ) {
        String normalizedClientId = InputSanitizer.trim(clientId);
        if (normalizedClientId == null || normalizedClientId.isEmpty()) {
            throw new InvalidRequestException("clientId is required");
        }
        if (!InputSanitizer.hasValidIdentifierFormat(normalizedClientId)) {
            throw new InvalidRequestException("clientId has an invalid format");
        }

        return getTransactionHistoryUseCase.execute(normalizedClientId).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }
}
