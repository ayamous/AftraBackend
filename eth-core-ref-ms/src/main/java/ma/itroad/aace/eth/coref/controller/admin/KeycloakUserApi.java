package ma.itroad.aace.eth.coref.controller.admin;

import ma.itroad.aace.eth.coref.model.bean.ResetPasswordRequest;
import ma.itroad.aace.eth.coref.security.keycloak.model.RoleBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface KeycloakUserApi {

    @GetMapping("{id}/roles")
    ResponseEntity<List<RoleBean>> getRolesByUserId(@PathVariable("id") String id);

    @PostMapping("{id}/roles")
    ResponseEntity<Void> assignRolesToUser(@PathVariable("id") String id, @RequestBody @Valid List<RoleBean> roleBeans);

    @DeleteMapping("{id}/roles")
    ResponseEntity<Void> unassignRolesToUser(@PathVariable("id") String id, @RequestBody @Valid List<RoleBean> roleBeans);

    @GetMapping("{id}/disable")
    ResponseEntity<Void> disableUser(@PathVariable("id") String id);

    @GetMapping("{id}/enable")
    ResponseEntity<Void> enableUser(@PathVariable("id") String id);

    @PutMapping("{id}/reset-password")
    ResponseEntity<Void> resetPassword(@PathVariable("id") String id, @RequestBody ResetPasswordRequest request);
}
