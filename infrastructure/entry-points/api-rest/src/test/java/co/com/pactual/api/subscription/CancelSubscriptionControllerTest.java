package co.com.pactual.api.subscription;

import co.com.pactual.api.exception.GlobalExceptionHandler;
import co.com.pactual.usecase.cancelsubscription.CancelSubscriptionUseCase;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionAlreadyCancelledException;
import co.com.pactual.usecase.cancelsubscription.exception.SubscriptionNotFoundException;
import co.com.pactual.usecase.subscribefund.SubscribeFundUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CancelSubscriptionControllerTest {

    @Mock
    private SubscribeFundUseCase subscribeFundUseCase;
    @Mock
    private CancelSubscriptionUseCase cancelSubscriptionUseCase;

    private SubscriptionController subscriptionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        subscriptionController = new SubscriptionController(subscribeFundUseCase, cancelSubscriptionUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnNoContentWhenCancellationSucceeds() throws Exception {
        mockMvc.perform(delete("/subscriptions/sub-001"))
                .andExpect(status().isNoContent());

        verify(cancelSubscriptionUseCase).execute("sub-001");
    }

    @Test
    void shouldReturnNotFoundWhenSubscriptionDoesNotExist() throws Exception {
        doThrow(new SubscriptionNotFoundException("sub-404"))
                .when(cancelSubscriptionUseCase)
                .execute("sub-404");

        mockMvc.perform(delete("/subscriptions/sub-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Suscripcion no encontrada con id sub-404"));
    }

    @Test
    void shouldReturnBadRequestWhenSubscriptionIsAlreadyCancelled() throws Exception {
        doThrow(new SubscriptionAlreadyCancelledException("sub-001"))
                .when(cancelSubscriptionUseCase)
                .execute("sub-001");

        mockMvc.perform(delete("/subscriptions/sub-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_ALREADY_CANCELLED"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La suscripcion sub-001 ya se encuentra cancelada"));
    }
}
