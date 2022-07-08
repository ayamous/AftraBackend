package ma.itroad.aace.eth.coref.security.keycloak.interceptor;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ma.itroad.aace.eth.coref.exception.UnauthorisedException;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class KeycloakInterceptor implements ClientHttpRequestInterceptor {
    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    @SneakyThrows
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken authenticationToken = (KeycloakAuthenticationToken) authentication;
            KeycloakPrincipal<RefreshableKeycloakSecurityContext> currentUser = (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) authenticationToken.getPrincipal();
            String token = currentUser.getKeycloakSecurityContext().getTokenString();
            String bearerToken = TOKEN_PREFIX.concat(token);
            httpRequest.getHeaders().add(AUTH_HEADER, bearerToken);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        } else {
            throw new UnauthorisedException("Unauthorized attempt", authentication.getClass().getName() + " is not an instance of KeycloakAuthenticationToken");
        }

    }
}
