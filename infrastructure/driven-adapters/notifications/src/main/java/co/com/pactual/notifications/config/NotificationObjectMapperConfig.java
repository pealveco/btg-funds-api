package co.com.pactual.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationObjectMapperConfig {

    @Bean
    public ObjectMapper notificationObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
