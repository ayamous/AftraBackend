package ma.itroad.aace.eth.coref.security.keycloak.service;

import edu.emory.mathcs.backport.java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import ma.itroad.aace.eth.core.common.EthClient;
import ma.itroad.aace.eth.core.common.api.messaging.domain.Ack;
import ma.itroad.aace.eth.core.common.api.messaging.domain.AppMessage;
import ma.itroad.aace.eth.core.common.api.messaging.enums.MessageType;
import ma.itroad.aace.eth.coref.exception.FunctionalError;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.model.enums.StatusUserAccount;
import ma.itroad.aace.eth.coref.repository.OrganizationRepository;
import ma.itroad.aace.eth.coref.repository.UserAccountRepository;
import ma.itroad.aace.eth.coref.security.keycloak.config.KeycloakHttpClient;
import ma.itroad.aace.eth.coref.security.keycloak.model.RoleBean;
import ma.itroad.aace.eth.coref.security.keycloak.model.UserBean;
import org.apache.logging.log4j.util.Strings;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KeycloakUserRestService extends KeycloakRestService<UserRepresentation, UserBean> {

    private static final String roleMappingUri = "/role-mappings/realm";
    private static final String clientRoleMappingUri = "/role-mappings/clients";
    private final UserAccountRepository repository;
    private final OrganizationRepository organizationRepository;
    private final String searchUri;
    private final String ethRealm;
    private final EthClient ethClient;
    private final KeycloakClientRestService clientRestService;

    protected KeycloakUserRestService(KeycloakHttpClient client, @Value("${eth.keycloak.user-uri}") String uri,
                                      @Value("${keycloak.realm}") String ethRealm,
                                      @Value("${eth.keycloak.search-uri}") String searchUri,
                                      UserAccountRepository repository, EthClient ethClient,
                                      KeycloakClientRestService clientRestService,
                                      OrganizationRepository organizationRepository) {
        super(client, uri);
        this.searchUri = searchUri;
        this.ethRealm = ethRealm;
        this.repository = repository;
        this.organizationRepository = organizationRepository;
        this.ethClient = ethClient;
        this.clientRestService = clientRestService;
    }

    @Override
    public UserRepresentation add(UserBean bean, Class<UserRepresentation> clazz) {
        // TODO handle retries
        bean.addRequiredAction("VERIFY_EMAIL");
        UserRepresentation representation = super.add(bean, clazz);
        findByUsername(bean.getUsername()).ifPresent(rep -> {
            UserAccount userAccount = new UserAccount();
            userAccount.setFirstName(rep.getFirstName());
            userAccount.setLogin(rep.getUsername());
            userAccount.setLastName(rep.getLastName());
            userAccount.setPassword(Strings.EMPTY);
            userAccount.setStatus(StatusUserAccount.ENABLED);
            userAccount.setEmail(rep.getEmail());
            userAccount.setPhoneNumber(bean.getPhoneNumber());
            userAccount.setReference(rep.getId());
            if (bean.getOrganization() != null) {
                userAccount.setOrganization(organizationRepository.findOneById(bean.getOrganization().getId()));
            }
            Optional<AppMessage> optionalAppMessage;
            try {
                repository.save(userAccount);
                Map<String, Object> params = new HashMap<>();
                AppMessage appMessage = new AppMessage();
                appMessage.setMessageId(UUID.randomUUID().toString());
                appMessage.setRecipient(userAccount.getEmail());
                appMessage.setType(MessageType.EMAIL_HTML);
                params.put("userFullName", rep.getFirstName().concat(" ").concat(rep.getLastName()));
                params.put("username", rep.getUsername());
                params.put("password", bean.getPassword());
                params.put("email", bean.getEmail());
                appMessage.setSubject("Création de compte ETHub");
                appMessage.addProperty(AppMessage.TEMPLATE_KEY_NAME, "new_account");
                appMessage.addProperty(AppMessage.TEMPLATE_PARAMS_KEY_NAME, params);
                optionalAppMessage = Optional.of(appMessage);
            } catch (Exception e) {
                log.error("Failed to persist the user {} internally", bean.getUsername(), e);
                deleteById(rep.getId());
                if (userAccount.getId() != null) {
                    repository.deleteById(userAccount.getId());
                }
                throw new FunctionalError("Failed to create the user");
            }
          try{
              optionalAppMessage.ifPresent(appMessage -> {
                  CompletableFuture<ResponseEntity<Void>> completableFuture = new CompletableFuture<>();
                  completableFuture.complete(ethClient.getMessaging().sendMailAsync(Collections.singletonList(appMessage)));
              });
          }catch(Exception ex){

          }
            representation.setId(rep.getId());
        });
        return representation;
    }

    @Override
    public UserRepresentation updateById(String id, UserBean bean, Class<UserRepresentation> clazz) {
        UserRepresentation request = bean.convert();
        HttpEntity<UserRepresentation> entity = new HttpEntity<>(request, headers);
        client.put(concatToUrl(id), entity, clazz);
        repository.findByReference(id).ifPresent(userAccount -> {
            userAccount.setLastName(bean.getLastName());
            userAccount.setFirstName(bean.getFirstName());
            userAccount.setPhoneNumber(bean.getPhoneNumber()!=null?bean.getPhoneNumber():userAccount.getPhoneNumber());
           if(bean.getOrganization() != null){
               userAccount.setOrganization(organizationRepository.findOneById(bean.getOrganization().getId()));
           }
            repository.save(userAccount);
        });
        return request;
    }

    public List<UserRepresentation> getAll() {
        return super.getAll(UserRepresentation[].class);
    }

    public Optional<UserRepresentation> findByUsername(String username) {
        ResponseEntity<UserRepresentation[]> response = client.get(uri.concat(searchUri.concat(String.format("%s", username))), UserRepresentation[].class);
        List<UserRepresentation> result = Arrays.asList(response.getBody());
        if (CollectionUtils.isEmpty(result)) {
            return Optional.empty();
        } else if (result.size() > 1) {
            log.warn("Multiple user found by the username {}", username);
            return Optional.empty();
        } else {
            return result.stream().findFirst();
        }
    }

    public void resetPassword(String userId, String newPassword, String currentPassword) {
        // TODO verify if the current password is correct
        CredentialRepresentation request = new CredentialRepresentation();
        request.setType("password");
        request.setValue(newPassword);
        HttpEntity<CredentialRepresentation> entity = new HttpEntity<>(request, headers);
        client.put(concatToUrl(userId).concat("/reset-password"), entity, void.class);
    }

    public void changeUserAccessibility(String userId, Boolean enabled) {
        // TODO verify if the current password is correct
        UserRepresentation request = getById(userId, UserRepresentation.class);
        request.setEnabled(enabled);
        HttpEntity<UserRepresentation> entity = new HttpEntity<>(request, headers);
        client.put(concatToUrl(userId), entity, UserRepresentation.class);
    }

    public List<RoleBean> getRolesByUserId(String id) {
        String clientId = clientRestService.getAdminRealmIdByClientId();
        List<RoleBean> roles = new ArrayList<>();
        ResponseEntity<RoleRepresentation[]> response = client.get(concatRoleMapping(id), RoleRepresentation[].class);
        if (response.getBody() != null) {
            roles.addAll(Arrays.asList(response.getBody()));
        }
        ResponseEntity<RoleRepresentation[]> adminResponse = client.get(concatClientRoleMapping(id, clientId).concat("/composite"), RoleRepresentation[].class);
        if (adminResponse.getBody() != null) {
            roles.addAll(Arrays.asList(adminResponse.getBody()));
        }
        return roles ;
    }

    public List<RoleRepresentation> assignRolesToUserById(String id, List<RoleBean> roleBeans) {
       return doSetRoles(id, roleBeans, true);
    }

    public void unassignRolesToUserById(String id, List<RoleBean> roleBeans) {
        doSetRoles(id, roleBeans, false);
    }

    public List<RoleRepresentation> doSetRoles(String id, List<RoleBean> roleBeans, boolean toBeAdded) {
        List<RoleRepresentation> request = roleBeans.stream().filter(roleBean -> roleBean.getContainerId() == null || roleBean.getContainerId().equalsIgnoreCase(ethRealm)).map(RoleBean::convert).collect(Collectors.toList());
        HttpEntity<List<RoleRepresentation>> entity = new HttpEntity<>(request, headers);
        if (toBeAdded) {
            client.post(concatRoleMapping(id), entity, void.class);

        } else {
            client.delete(concatRoleMapping(id), entity);
        }
        Map<String, List<RoleBean>> clientRoleRequest = roleBeans.stream().filter(e -> e.getContainerId() != null && !e.getContainerId().equalsIgnoreCase(ethRealm)).collect(Collectors.groupingBy(RoleBean::getContainerId));
        clientRoleRequest.forEach((clientId, roles) -> {
            HttpEntity<List<RoleRepresentation>> httpEntity = new HttpEntity<>(roles.stream().map(RoleBean::convert).collect(Collectors.toList()), headers);
            if (toBeAdded) {
                client.post(concatClientRoleMapping(id, clientId), httpEntity, void.class);

            } else {
                client.delete(concatClientRoleMapping(id, clientId), httpEntity);
            }
        });
        return request;
    }

    private String concatRoleMapping(String value) {
        return uri.concat(String.format("/%s", value).concat(roleMappingUri));
    }
    private String concatClientRoleMapping(String userId, String clientId) {
        return uri.concat(String.format("/%s", userId).concat(clientRoleMappingUri)).concat(String.format("/%s", clientId));
    }
}
