package ma.itroad.aace.eth.coref.model.bean;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class ESafeDocumentSharingRequest {
    /**
     * Set of users id
     */
    private Set<Long> users;

    /**
     * Set of organization id
     */
    private Set<Long> organizations;

    /**
     * document id to be shared
     */
    @NotNull
    private Long documentId;

    public void addUserId(Long userId) {
        if (CollectionUtils.isEmpty(users)) {
            users = new HashSet<>();
        }
        users.add(userId);
    }

    public void addUserIds(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        if (CollectionUtils.isEmpty(users)) {
            users = new HashSet<>();
        }
        users.addAll(userIds);
    }
}
