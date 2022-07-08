package ma.itroad.aace.eth.coref.security.keycloak.service;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.coref.security.keycloak.config.KeycloakHttpClient;
import ma.itroad.aace.eth.coref.security.keycloak.model.ClientBean;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeycloakClientRestService extends KeycloakRestService<ClientRepresentation, ClientBean> {

    private final String adminRealm;
    private final String ethRealm;

    protected KeycloakClientRestService(KeycloakHttpClient client, @Value("${eth.keycloak.client-uri}") String uri,
                                        @Value("${keycloak.realm}") String ethRealm,
                                        @Value("${eth.keycloak.admin-realm}") String adminRealm) {
        super(client, uri);
        this.adminRealm = adminRealm;
        this.ethRealm = ethRealm;
    }

    public String getIdByClientId(String clientId) {
        ClientRepresentation representation = getAll(ClientRepresentation[].class).stream().filter(clientRepresentation -> clientRepresentation.getClientId().equalsIgnoreCase(clientId)).findFirst().orElse(null);
        return representation == null ? null : representation.getId();
    }

    public String getAdminRealmIdByClientId() {
        ClientRepresentation representation = getAll(ClientRepresentation[].class).stream().filter(clientRepresentation -> clientRepresentation.getClientId().equalsIgnoreCase(adminRealm)).findFirst().orElse(null);
        return representation == null ? null : representation.getId();
    }

    public List<RoleRepresentation> getRolesByClientId(String clientId) {
        String id = getIdByClientId(clientId);
        if (id != null) {
            ResponseEntity<RoleRepresentation[]> response = client.get(concatToUrl(id).concat("/roles"), RoleRepresentation[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
