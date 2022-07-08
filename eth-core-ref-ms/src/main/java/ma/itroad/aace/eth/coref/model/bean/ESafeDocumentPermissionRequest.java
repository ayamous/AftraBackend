package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
public class ESafeDocumentPermissionRequest {
    private Set<Long> organizations;
    private Set<Long> countries;

    @JsonIgnore
    private boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(organizations) || CollectionUtils.isNotEmpty(countries);
    }

    public void addOrganization(Long organizationId) {
        if (CollectionUtils.isEmpty(organizations)) {
            organizations = new HashSet<>();
        }
        organizations.add(organizationId);
    }

    public void addOrganizations(Collection<Long> organizationIds) {
        if (CollectionUtils.isEmpty(organizationIds)) {
            return;
        }
        if (CollectionUtils.isEmpty(organizations)) {
            organizations = new HashSet<>();
        }
        organizations.addAll(organizationIds);
    }
}
