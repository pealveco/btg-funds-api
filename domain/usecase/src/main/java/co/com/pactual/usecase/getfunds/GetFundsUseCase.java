package co.com.pactual.usecase.getfunds;

import co.com.pactual.model.fund.Fund;
import co.com.pactual.model.fund.gateways.FundRepository;
import co.com.pactual.usecase.getfunds.exception.FundsRetrievalException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetFundsUseCase {

    private final FundRepository fundRepository;

    public List<Fund> execute() {
        try {
            return fundRepository.findAll();
        } catch (RuntimeException exception) {
            throw new FundsRetrievalException("Could not retrieve available funds", exception);
        }
    }
}
