package ma.itroad.aace.eth.coref.controller.admin.impl;

import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.coref.controller.admin.AdminApi;
import ma.itroad.aace.eth.coref.controller.admin.KeycloakUserApi;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.model.bean.ResetPasswordRequest;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import ma.itroad.aace.eth.coref.repository.UserAccountRepository;
import ma.itroad.aace.eth.coref.security.keycloak.model.RoleBean;
import ma.itroad.aace.eth.coref.security.keycloak.model.UserBean;
import ma.itroad.aace.eth.coref.security.keycloak.service.KeycloakUserRestService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("admin/users")
public class KeycloakUserController implements AdminApi<UserBean, String>, KeycloakUserApi {
    private final KeycloakUserRestService restService;
    private final UserAccountRepository repository;

    @Override
    public ResponseEntity add(UserBean domain) {

        try{
            UserRepresentation result = restService.add(domain, UserRepresentation.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserBean.of(result));
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity deleteById(String s) {
        try{
            restService.deleteById(s);
            return ResponseEntity.ok().body(s);
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity getAll() {
        try{
            List<UserBean> result= UserBean.of(restService.getAll());
            result.forEach(this::setUserOrganization);
            return ResponseEntity.ok(result);
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    private void setUserOrganization(UserBean userBean) {
        repository.findByReference(userBean.getId()).ifPresent(userAccount -> {
            if(userAccount.getOrganization() == null) {
                return;
            }
            Organization organization = userAccount.getOrganization();
            OrganizationBean organizationBean = new OrganizationBean();
            organizationBean.setAcronym(organization.getAcronym());
            organizationBean.setId(organization.getId());
            organizationBean.setReference(organization.getReference());
            organizationBean.setCreatedBy(organization.getCreatedBy());
            organizationBean.setCreatedOn(organization.getCreatedOn());
            organizationBean.setName(organization.getName());
            userBean.setOrganization(organizationBean);
        });
    }

    @Override
    public ResponseEntity getOneById(String s) {

        try{
            UserBean result = UserBean.of(restService.getById(s, UserRepresentation.class));
            setUserOrganization(result);
            return ResponseEntity.ok(result);
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity updateById(String id, UserBean domain) {

        try{
            restService.updateById(id, domain, UserRepresentation.class);
            return ResponseEntity.ok().body(domain);
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }

    }

    @Override
    public ResponseEntity<List<RoleBean>> getRolesByUserId(String id) {
        return ResponseEntity.ok(restService.getRolesByUserId(id));
    }

    @Override
    public ResponseEntity<Void> assignRolesToUser(String id, @Valid List<RoleBean> roleBeans) {
        restService.assignRolesToUserById(id, roleBeans);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unassignRolesToUser(String id, @Valid List<RoleBean> roleBeans) {
        restService.unassignRolesToUserById(id, roleBeans);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> disableUser(String id) {
        restService.changeUserAccessibility(id, false);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> enableUser(String id) {
        restService.changeUserAccessibility(id, true);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(String id, ResetPasswordRequest request) {
        restService.resetPassword(id, request.getNewPassword(), request.getCurrentPassword());
        return ResponseEntity.noContent().build();
    }


}
