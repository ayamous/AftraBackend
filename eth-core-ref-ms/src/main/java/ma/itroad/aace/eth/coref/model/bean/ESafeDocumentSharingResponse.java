package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentSharingState;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ESafeDocumentSharingResponse {

    private Set<ESafeDocumentSharingResponseItem> response;

    public void addItem(Long documentId, Long userId, Long organizationId, ESafeDocumentSharingState shared, String reason) {
        if (CollectionUtils.isEmpty(response)) {
            response = new HashSet<>();
        }
        response.add(new ESafeDocumentSharingResponseItem(documentId, userId, organizationId, shared, reason));
    }


    public void addSharedItem(Long documentId, Long userId, Long organizationId) {
        addItem(documentId, userId, organizationId, ESafeDocumentSharingState.SHARED, null);
    }

    @Data
    @AllArgsConstructor
    class ESafeDocumentSharingResponseItem {
        private Long documentId;
        private Long userId;
        private Long organizationId;
        private ESafeDocumentSharingState state;
        private String reason;
    }
}
