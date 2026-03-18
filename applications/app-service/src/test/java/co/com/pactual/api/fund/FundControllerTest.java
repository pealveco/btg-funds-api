package co.com.pactual.api.fund;

import co.com.pactual.model.fund.Fund;
import co.com.pactual.usecase.getfunds.GetFundsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FundControllerTest {

    @Mock
    private GetFundsUseCase getFundsUseCase;

    @InjectMocks
    private FundController fundController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fundController).build();
    }

    @Test
    void shouldReturnFunds() throws Exception {
        when(getFundsUseCase.execute()).thenReturn(List.of(
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
        ));

        mockMvc.perform(get("/funds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fundId").value("1"))
                .andExpect(jsonPath("$[0].name").value("FPV_BTG_PACTUAL_RECAUDADORA"))
                .andExpect(jsonPath("$[0].minimumAmount").value(75000))
                .andExpect(jsonPath("$[0].category").value("FPV"));
    }
}
