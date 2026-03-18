package co.com.pactual.usecase.getfunds;

import co.com.pactual.model.fund.Fund;
import co.com.pactual.model.fund.gateways.FundRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetFundsUseCase {

    private final FundRepository fundRepository;

    public List<Fund> execute() {
        return fundRepository.findAll();
    }
}
