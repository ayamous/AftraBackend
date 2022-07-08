package ma.itroad.aace.eth.coref.security.keycloak.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.util.CollectionUtils;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserBean implements KeycloakRepresentationModel<UserRepresentation> {
    private String id;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String phoneNumber;
    private Boolean enabled;
    private Boolean emailVerified;
    @NotNull
    @Email
    private String email;
    private OrganizationBean organization;

    private byte[] avatar;

    @JsonIgnore
    private Set<String> requiredActions;

    public static UserBean of(UserRepresentation representation) {
        return UserBean.builder()
                .id(representation.getId())
                .username(representation.getUsername())
                .firstName(representation.getFirstName())
                .lastName(representation.getLastName())
                .enabled(representation.isEnabled())
                .emailVerified(representation.isEmailVerified())
                .email(representation.getEmail())
                .build();
    }

    public static List<UserBean> of(List<UserRepresentation> representations) {
        if (CollectionUtils.isEmpty(representations))
            return new ArrayList<>();
        return representations.stream().map(UserBean::of).collect(Collectors.toList());
    }


    @Override
    public UserRepresentation convert() {
        UserRepresentation representation = new UserRepresentation();
        CredentialRepresentation passwordCredential = new CredentialRepresentation();
        passwordCredential.setType(CredentialRepresentation.PASSWORD);
        passwordCredential.setValue(this.password);
        representation.setId(this.getId());
        representation.setUsername(this.getUsername());
        representation.setCredentials(ImmutableList.of(passwordCredential));
        representation.setEmail(this.getEmail());
        representation.setEmailVerified(false);
        representation.setFirstName(this.firstName);
        representation.setLastName(this.getLastName());
        representation.setEnabled(true);
        return representation;
    }

    @JsonIgnore
    public void addRequiredAction(String requiredAction) {
        if(requiredActions == null) {
            requiredActions = new HashSet<>();
        }
        requiredActions.add(requiredAction);
    }
}
