package ma.itroad.aace.eth.coref.security.keycloak.service;

import edu.emory.mathcs.backport.java.util.Arrays;
import ma.itroad.aace.eth.coref.security.keycloak.config.KeycloakHttpClient;
import ma.itroad.aace.eth.coref.security.keycloak.model.KeycloakRepresentationModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class KeycloakRestService<R, T extends KeycloakRepresentationModel<R>> {

    protected final KeycloakHttpClient client;
    protected final HttpHeaders headers;
    protected final String uri;
    protected String byIdUri;

    protected KeycloakRestService(KeycloakHttpClient client, String uri) {
        this.client = client;
        this.headers = new HttpHeaders();
        this.uri = uri;
        this.byIdUri = uri;
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public R add(T bean, Class<R> clazz) {
        R request = bean.convert();
        HttpEntity<R> entity = new HttpEntity<>(request, headers);
        ResponseEntity<R> response = client.post(uri, entity, clazz);
        return request;
    }

    public R updateById(String id, T bean, Class<R> clazz) {
        R request = bean.convert();
        HttpEntity<R> entity = new HttpEntity<>(request, headers);
        client.put(concatToUrl(id), entity, clazz);
        return request;
    }

    public List<R> getAll(Class<R[]> clazz) {
        ResponseEntity<R[]> response = client.get(uri, clazz);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : new ArrayList<>();
    }

    public List<R> getAll(Class<R[]> clazz, String uri) {
        ResponseEntity<R[]> response = client.get(uri, clazz);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : new ArrayList<>();
    }

    public R getById(String id, Class<R> clazz) {
        ResponseEntity<R> response = client.get(concatToUrl(id), clazz);
        return response.getBody();
    }

    public void deleteById(String id) {
        client.delete(concatToUrl(id));
    }

    public String concatToUrl(String value) {
        return byIdUri.concat(String.format("/%s", value));
    }

}
