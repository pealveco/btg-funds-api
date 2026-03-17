package co.com.pactual.model.fund;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Fund {
    private String fundId;
    private String name;
    private BigDecimal minimumAmount;
    private String category;
}
