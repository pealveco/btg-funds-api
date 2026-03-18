package co.com.pactual.api.subscription;

import co.com.pactual.api.exception.GlobalExceptionHandler;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.usecase.subscribefund.SubscribeFundUseCase;
import co.com.pactual.usecase.subscribefund.exception.ClientNotFoundException;
import co.com.pactual.usecase.subscribefund.exception.MinimumSubscriptionAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscribeFundUseCase subscribeFundUseCase;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateSubscription() throws Exception {
        when(subscribeFundUseCase.execute(eq("client-001"), eq("fund-001"), eq(BigDecimal.valueOf(100_000L))))
                .thenReturn(Subscription.builder()
                        .subscriptionId("sub-001")
                        .clientId("client-001")
                        .fundId("fund-001")
                        .amount(BigDecimal.valueOf(100_000L))
                        .status(SubscriptionStatus.ACTIVE)
                        .createdAt(LocalDateTime.of(2026, 3, 18, 0, 0))
                        .build());

        mockMvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "client-001",
                                  "fundId": "fund-001",
                                  "amount": 100000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscriptionId").value("sub-001"))
                .andExpect(jsonPath("$.clientId").value("client-001"))
                .andExpect(jsonPath("$.fundId").value("fund-001"))
                .andExpect(jsonPath("$.amount").value(100000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
        when(subscribeFundUseCase.execute(any(), any(), any()))
                .thenThrow(new ClientNotFoundException("client-001"));

        mockMvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "client-001",
                                  "fundId": "fund-001",
                                  "amount": 100000
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsBelowMinimum() throws Exception {
        when(subscribeFundUseCase.execute(any(), any(), any()))
                .thenThrow(new MinimumSubscriptionAmountException("FPV_TEST"));

        mockMvc.perform(post("/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "client-001",
                                  "fundId": "fund-001",
                                  "amount": 1000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El monto de la suscripcion no cumple el minimo del fondo FPV_TEST"));
    }
}
