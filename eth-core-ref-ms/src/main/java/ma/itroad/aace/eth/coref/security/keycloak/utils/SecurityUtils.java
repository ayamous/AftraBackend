package ma.itroad.aace.eth.coref.security.keycloak.utils;

import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public abstract class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<KeycloakAuthenticationToken> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof KeycloakAuthenticationToken ? Optional.of((KeycloakAuthenticationToken) auth) : Optional.empty();
    }

    public static Optional<KeycloakPrincipal<RefreshableKeycloakSecurityContext>> getCurrentPrincipal() {
        Optional<KeycloakAuthenticationToken> value = getAuthentication();
        return value.filter(keycloakAuthenticationToken ->
                keycloakAuthenticationToken.getPrincipal() instanceof KeycloakPrincipal).map(keycloakAuthenticationToken ->
                (KeycloakPrincipal<RefreshableKeycloakSecurityContext>) keycloakAuthenticationToken.getPrincipal());
    }

    public static Optional<UserAccount> getCurrentUser() {
        Optional<KeycloakPrincipal<RefreshableKeycloakSecurityContext>> value = getCurrentPrincipal();
        if (value.isPresent()) {
            AccessToken accessToken = value.get().getKeycloakSecurityContext().getToken();
            UserAccount userAccount = new UserAccount();
            userAccount.setReference(accessToken.getSubject());
            userAccount.setLogin(accessToken.getPreferredUsername());
            userAccount.setFirstName(accessToken.getGivenName());
            userAccount.setLastName(accessToken.getFamilyName());
            userAccount.setEmail(accessToken.getEmail());
            return Optional.of(userAccount);
        } else {
            return Optional.empty();
        }
    }

    public static <T extends AuditEntity> T setActionBy(T auditEntity, boolean isCreation) {
        if (auditEntity == null)
            return null;
        getCurrentUser().ifPresent(userAccount -> {
            if (isCreation) {
                auditEntity.setCreatedBy(userAccount.getReference());
            } else {
                auditEntity.setUpdatedBy(userAccount.getReference());
            }
        });
        return auditEntity;
    }


}
