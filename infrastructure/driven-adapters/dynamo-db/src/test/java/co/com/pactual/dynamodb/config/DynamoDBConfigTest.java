package co.com.pactual.dynamodb.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamoDBConfigTest {

    private final DynamoDBConfig config = new DynamoDBConfig();

    @Test
    void shouldCreateDynamoDbClientWithoutEndpointOverride() {
        DynamoDbClient client = config.dynamoDbClient("us-east-1", "");

        assertEquals(Region.US_EAST_1, client.serviceClientConfiguration().region());
        assertTrue(client.serviceClientConfiguration().endpointOverride().isEmpty());
    }

    @Test
    void shouldCreateDynamoDbClientWithEndpointOverride() {
        DynamoDbClient client = config.dynamoDbClient("us-east-1", "http://localhost:8000");

        assertEquals(Region.US_EAST_1, client.serviceClientConfiguration().region());
        assertEquals(URI.create("http://localhost:8000"), client.serviceClientConfiguration().endpointOverride().orElseThrow());
    }

    @Test
    void shouldCreateEnhancedClient() {
        DynamoDbClient client = config.dynamoDbClient("us-east-1", "");

        DynamoDbEnhancedClient enhancedClient = config.getDynamoDbEnhancedClient(client);

        assertNotNull(enhancedClient);
    }
}
