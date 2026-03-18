package co.com.pactual.config;

import co.com.pactual.model.subscription.Subscription;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Aspect
@Component
public class UseCaseLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(UseCaseLoggingAspect.class);

    @Around("execution(public * co.com.pactual.usecase..*UseCase.execute(..))")
    public Object logUseCaseExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String useCaseName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        Object[] arguments = joinPoint.getArgs();

        LOGGER.info("Use case started. useCase={} {}", useCaseName, buildStartContext(useCaseName, arguments));
        try {
            Object result = joinPoint.proceed();
            LOGGER.info("Use case completed successfully. useCase={} {}", useCaseName, buildSuccessContext(useCaseName, arguments, result));
            return result;
        } catch (Throwable throwable) {
            if (isControlledException(throwable)) {
                LOGGER.warn("Use case completed with controlled error. useCase={} {} message={}",
                        useCaseName,
                        buildFailureContext(useCaseName, arguments),
                        throwable.getMessage());
            } else {
                LOGGER.error("Use case failed unexpectedly. useCase={} {}", useCaseName, buildFailureContext(useCaseName, arguments), throwable);
            }
            throw throwable;
        }
    }

    private String buildStartContext(String useCaseName, Object[] arguments) {
        return switch (useCaseName) {
            case "SubscribeFundUseCase" -> "clientId=" + argument(arguments, 0)
                    + " fundId=" + argument(arguments, 1)
                    + " amount=" + argument(arguments, 2);
            case "CancelSubscriptionUseCase" -> "subscriptionId=" + argument(arguments, 0);
            case "GetTransactionHistoryUseCase" -> "clientId=" + argument(arguments, 0);
            case "GetFundsUseCase" -> "operation=listFunds";
            default -> "method=execute";
        };
    }

    private String buildSuccessContext(String useCaseName, Object[] arguments, Object result) {
        return switch (useCaseName) {
            case "SubscribeFundUseCase" -> {
                Subscription subscription = (Subscription) result;
                yield "clientId=" + subscription.getClientId()
                        + " fundId=" + subscription.getFundId()
                        + " subscriptionId=" + subscription.getSubscriptionId()
                        + " status=" + subscription.getStatus();
            }
            case "CancelSubscriptionUseCase" -> "subscriptionId=" + argument(arguments, 0);
            case "GetTransactionHistoryUseCase" -> "clientId=" + argument(arguments, 0)
                    + " transactionsCount=" + sizeOf(result);
            case "GetFundsUseCase" -> "fundsCount=" + sizeOf(result);
            default -> "resultType=" + (result != null ? result.getClass().getSimpleName() : "void");
        };
    }

    private String buildFailureContext(String useCaseName, Object[] arguments) {
        return switch (useCaseName) {
            case "SubscribeFundUseCase" -> "clientId=" + argument(arguments, 0)
                    + " fundId=" + argument(arguments, 1)
                    + " amount=" + argument(arguments, 2);
            case "CancelSubscriptionUseCase" -> "subscriptionId=" + argument(arguments, 0);
            case "GetTransactionHistoryUseCase" -> "clientId=" + argument(arguments, 0);
            case "GetFundsUseCase" -> "operation=listFunds";
            default -> "argumentsCount=" + arguments.length;
        };
    }

    private boolean isControlledException(Throwable throwable) {
        String simpleName = throwable.getClass().getSimpleName();
        return throwable.getClass().getPackageName().contains(".usecase.")
                && !simpleName.endsWith("PersistenceException")
                && !simpleName.endsWith("RetrievalException");
    }

    private Object argument(Object[] arguments, int index) {
        return arguments.length > index ? arguments[index] : null;
    }

    private int sizeOf(Object result) {
        return result instanceof List<?> list ? list.size() : 0;
    }
}
