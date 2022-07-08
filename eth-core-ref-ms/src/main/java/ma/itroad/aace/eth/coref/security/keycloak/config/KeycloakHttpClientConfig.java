package ma.itroad.aace.eth.coref.security.keycloak.config;

import ma.itroad.aace.eth.coref.security.keycloak.interceptor.KeycloakInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class KeycloakHttpClientConfig {

    @Bean(name = "keycloakClient")
    public RestTemplate keycloakHttpClient(KeycloakInterceptor interceptor) {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
