package co.com.pactual.model.client.gateways;

import co.com.pactual.model.client.Client;

import java.util.Optional;

public interface ClientRepository {
    Optional<Client> findById(String clientId);
    Client save(Client client);
}
