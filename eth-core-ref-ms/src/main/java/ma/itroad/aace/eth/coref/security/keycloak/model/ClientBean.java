package ma.itroad.aace.eth.coref.security.keycloak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientBean implements KeycloakRepresentationModel<ClientRepresentation> {

    private String id;
    private String clientId;
    @NotNull
    private String name;
    private String description;
    private boolean enabled;

    public static ClientBean of(ClientRepresentation representation) {
        return ClientBean.builder()
                .id(representation.getId())
                .clientId(representation.getClientId())
                .name(representation.getName())
                .enabled(representation.isEnabled())
                .build();
    }

    public static List<ClientBean> of(List<ClientRepresentation> representations) {
        if (CollectionUtils.isEmpty(representations))
            return new ArrayList<>();
        return representations.stream().map(ClientBean::of).collect(Collectors.toList());
    }

    @Override
    public ClientRepresentation convert() {
        ClientRepresentation representation = new ClientRepresentation();
        representation.setId(getId());
        representation.setDescription(getDescription());
        representation.setName(getName());
        representation.setClientId(getClientId());
        representation.setEnabled(isEnabled());
        return representation;
    }
}
