package ma.itroad.aace.eth.coref.security.keycloak.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KeycloakHttpClient {
    private final RestTemplate keycloakRestTemplate;

    public KeycloakHttpClient(@Qualifier("keycloakClient") RestTemplate restTemplate) {
        keycloakRestTemplate = restTemplate;
    }

    public <R, T> ResponseEntity<T> post(String urlVariable, R object, Class<T> clazz) {
        log.info("Executing a POST request to {}", urlVariable);
        return keycloakRestTemplate.postForEntity(urlVariable, object, clazz);
    }

    public <R, T> void put(String urlVariable, R object, Class<T> clazz) {
        log.info("Executing a PUT request to {}", urlVariable);
        keycloakRestTemplate.put(urlVariable, object, clazz);
    }

    public <T> ResponseEntity<T> get(String urlVariable, Class<T> clazz) {
        log.info("Executing a GET request to {}", urlVariable);
        return keycloakRestTemplate.getForEntity(urlVariable, clazz);
    }

    public void delete(String urlVariable) {
        log.info("Executing a DELETE request to {}", urlVariable);
        keycloakRestTemplate.delete(urlVariable);
    }

    public void delete(String urlVariable, HttpEntity<?> request) {
        log.info("Executing a DELETE request to {}", urlVariable);
        keycloakRestTemplate.exchange(urlVariable, HttpMethod.DELETE, request, void.class);
    }
}
