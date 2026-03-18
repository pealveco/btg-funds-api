package co.com.pactual.usecase.getfunds;

import co.com.pactual.model.fund.Fund;
import co.com.pactual.model.fund.gateways.FundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFundsUseCaseTest {

    @Mock
    private FundRepository fundRepository;

    @InjectMocks
    private GetFundsUseCase useCase;

    @Test
    void shouldReturnAllFunds() {
        List<Fund> expectedFunds = List.of(
                Fund.builder()
                        .fundId("1")
                        .name("FPV_BTG_PACTUAL_RECAUDADORA")
                        .minimumAmount(BigDecimal.valueOf(75_000L))
                        .category("FPV")
                        .build(),
                Fund.builder()
                        .fundId("2")
                        .name("FPV_BTG_PACTUAL_ECOPETROL")
                        .minimumAmount(BigDecimal.valueOf(125_000L))
                        .category("FPV")
                        .build()
        );

        when(fundRepository.findAll()).thenReturn(expectedFunds);

        List<Fund> funds = useCase.execute();

        assertEquals(expectedFunds, funds);
        verify(fundRepository).findAll();
    }
}
