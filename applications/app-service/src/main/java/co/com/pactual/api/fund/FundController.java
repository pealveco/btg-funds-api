package co.com.pactual.api.fund;

import co.com.pactual.api.fund.dto.FundResponse;
import co.com.pactual.usecase.getfunds.GetFundsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/funds")
@RequiredArgsConstructor
public class FundController {

    private final GetFundsUseCase getFundsUseCase;

    @GetMapping
    public List<FundResponse> getFunds() {
        return getFundsUseCase.execute()
                .stream()
                .map(FundResponse::from)
                .toList();
    }
}
