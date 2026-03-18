package co.com.pactual.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import co.com.pactual.model.enums.SubscriptionStatus;
import co.com.pactual.model.subscription.Subscription;
import co.com.pactual.usecase.subscribefund.exception.ClientNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseCaseLoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private UseCaseLoggingAspect aspect;
    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        aspect = new UseCaseLoggingAspect();
        logger = (Logger) LoggerFactory.getLogger(UseCaseLoggingAspect.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void shouldLogSuccessfulSubscriptionUseCase() throws Throwable {
        Subscription result = Subscription.builder()
                .subscriptionId("sub-001")
                .clientId("client-001")
                .fundId("fund-001")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) SubscribeFundUseCase.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"client-001", "fund-001", BigDecimal.valueOf(100_000L)});
        when(joinPoint.proceed()).thenReturn(result);

        Object returned = aspect.logUseCaseExecution(joinPoint);

        assertSame(result, returned);
        assertEquals(2, appender.list.size());
        assertEquals(Level.INFO, appender.list.get(0).getLevel());
        assertEquals(Level.INFO, appender.list.get(1).getLevel());
    }

    @Test
    void shouldLogControlledExceptionAsWarn() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) CancelSubscriptionUseCase.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"sub-001"});
        when(joinPoint.proceed()).thenThrow(new ClientNotFoundException("client-001"));

        assertThrows(ClientNotFoundException.class, () -> aspect.logUseCaseExecution(joinPoint));

        assertEquals(2, appender.list.size());
        assertEquals(Level.INFO, appender.list.get(0).getLevel());
        assertEquals(Level.WARN, appender.list.get(1).getLevel());
    }

    @Test
    void shouldLogUnexpectedExceptionAsError() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) GetFundsUseCase.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> aspect.logUseCaseExecution(joinPoint));

        assertEquals(2, appender.list.size());
        assertEquals(Level.INFO, appender.list.get(0).getLevel());
        assertEquals(Level.ERROR, appender.list.get(1).getLevel());
    }

    @Test
    void shouldLogFundsUseCaseCount() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) GetTransactionHistoryUseCase.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"client-001"});
        when(joinPoint.proceed()).thenReturn(List.of("tx-1", "tx-2"));

        Object returned = aspect.logUseCaseExecution(joinPoint);

        assertEquals(List.of("tx-1", "tx-2"), returned);
        assertEquals(2, appender.list.size());
        assertEquals(Level.INFO, appender.list.get(1).getLevel());
    }

    static class SubscribeFundUseCase {
    }

    static class CancelSubscriptionUseCase {
    }

    static class GetFundsUseCase {
    }

    static class GetTransactionHistoryUseCase {
    }
}
