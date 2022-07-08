package ma.itroad.aace.eth.coref.security.keycloak.service;

import ma.itroad.aace.eth.coref.security.keycloak.config.KeycloakHttpClient;
import ma.itroad.aace.eth.coref.security.keycloak.model.RoleBean;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeycloakRoleRestService extends KeycloakRestService<RoleRepresentation, RoleBean> {

    private final String adminRealm;
    private final String ethRealm;
    private final KeycloakClientRestService clientRestService;
    protected KeycloakRoleRestService(KeycloakHttpClient client, @Value("${eth.keycloak.role-uri}") String uri, @Value("${eth.keycloak.role-by-id-uri}") String byIdUri,
                                      @Value("${keycloak.realm}") String ethRealm,
                                      @Value("${eth.keycloak.admin-realm}") String adminRealm,
                                      KeycloakClientRestService clientRestService) {
        super(client, uri);
        this.byIdUri = byIdUri;
        this.adminRealm = adminRealm;
        this.ethRealm = ethRealm;
        this.clientRestService = clientRestService;
    }

    public List<RoleRepresentation> getAll() {
        List<RoleRepresentation> roles = new ArrayList<>();
        roles.addAll(super.getAll(RoleRepresentation[].class));
        roles.addAll(clientRestService.getRolesByClientId(adminRealm));
        return roles;
    }

    @Override
    public RoleRepresentation getById(String id, Class<RoleRepresentation> clazz) {
        RoleRepresentation roleRepresentation = super.getById(id, clazz);
        if(roleRepresentation == null) {
            this.byIdUri = this.byIdUri.replace(ethRealm, adminRealm);
            roleRepresentation = super.getById(id, clazz);
            this.byIdUri = this.byIdUri.replace(adminRealm, ethRealm);
        }
        return roleRepresentation;
    }
}
