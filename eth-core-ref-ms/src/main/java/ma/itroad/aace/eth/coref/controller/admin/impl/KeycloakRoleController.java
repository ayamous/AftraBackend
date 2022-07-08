package ma.itroad.aace.eth.coref.controller.admin.impl;

import lombok.AllArgsConstructor;
import ma.itroad.aace.eth.coref.controller.admin.AdminApi;
import ma.itroad.aace.eth.coref.security.keycloak.model.RoleBean;
import ma.itroad.aace.eth.coref.security.keycloak.model.UserBean;
import ma.itroad.aace.eth.coref.security.keycloak.service.KeycloakRoleRestService;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@AllArgsConstructor
@RequestMapping("admin/roles")
public class KeycloakRoleController implements AdminApi<RoleBean, String> {

    private final KeycloakRoleRestService restService;

    @Override
    public ResponseEntity add(RoleBean domain) {

        try{
            restService.add(domain, RoleRepresentation.class);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity deleteById(String id) {

        try{
            restService.deleteById(id);
            return ResponseEntity.ok().body("deleted successfully");
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity getAll() {
        try{
            return ResponseEntity.ok(RoleBean.of(restService.getAll()));

        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity getOneById(String id) {
        try{
            return ResponseEntity.ok(RoleBean.of(restService.getById(id, RoleRepresentation.class)));

        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }

    @Override
    public ResponseEntity updateById(String id, RoleBean domain) {
        try{
            restService.updateById(id, domain, RoleRepresentation.class);
            return ResponseEntity.ok().body(domain);
        }catch (HttpClientErrorException ex){
            return ResponseEntity.status(ex.getRawStatusCode()).body(ex.getMessage());
        }
    }
}
