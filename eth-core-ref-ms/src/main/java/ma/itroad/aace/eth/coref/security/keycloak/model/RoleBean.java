package ma.itroad.aace.eth.coref.security.keycloak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleBean implements KeycloakRepresentationModel<RoleRepresentation> {

    private String id;
    @NotNull
    private String name;
    private String description;
    private Boolean clientRole;
    private boolean composite;
    private String containerId;

    public static RoleBean of(RoleRepresentation representation) {
        return RoleBean.builder()
                .id(representation.getId())
                .name(representation.getName())
                .description(representation.getDescription())
                .clientRole(representation.getClientRole())
                .containerId(representation.getContainerId())
                .composite(representation.isComposite())
                .build();
    }

    public static List<RoleBean> of(List<RoleRepresentation> representations) {
        if (CollectionUtils.isEmpty(representations))
            return new ArrayList<>();
        return representations.stream().map(RoleBean::of).collect(Collectors.toList());
    }

    @Override
    public RoleRepresentation convert() {
        RoleRepresentation representation = new RoleRepresentation();
        representation.setId(this.getId());
        representation.setDescription(this.getDescription());
        representation.setName(this.getName());
        representation.setContainerId(this.containerId);
        representation.setClientRole(this.clientRole);
        representation.setComposite(this.isComposite());
        return representation;
    }
}
