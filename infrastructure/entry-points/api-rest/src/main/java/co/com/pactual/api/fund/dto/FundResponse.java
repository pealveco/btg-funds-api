package co.com.pactual.api.fund.dto;

import co.com.pactual.model.fund.Fund;

import java.math.BigDecimal;

public record FundResponse(
        String fundId,
        String name,
        BigDecimal minimumAmount,
        String category
) {
    public static FundResponse from(Fund fund) {
        return new FundResponse(
                fund.getFundId(),
                fund.getName(),
                fund.getMinimumAmount(),
                fund.getCategory()
        );
    }
}
