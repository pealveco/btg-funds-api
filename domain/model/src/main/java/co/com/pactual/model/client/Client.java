package co.com.pactual.model.client;
import co.com.pactual.model.enums.NotificationChannel;
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
public class Client {
    private String clientId;
    private String name;
    private String email;
    private String phone;
    private NotificationChannel notificationPreference;
    private BigDecimal availableBalance;
}
