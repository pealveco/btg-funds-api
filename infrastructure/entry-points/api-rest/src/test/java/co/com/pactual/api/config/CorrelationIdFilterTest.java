package co.com.pactual.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorrelationIdFilterTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void shouldReuseCorrelationIdFromRequest() throws Exception {
        mockMvc.perform(get("/test").header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsAbsent() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, not(isEmptyOrNullString())));
    }

    @RestController
    static class TestController {

        @GetMapping("/test")
        ResponseEntity<Void> test() {
            return ResponseEntity.ok().build();
        }
    }
}
