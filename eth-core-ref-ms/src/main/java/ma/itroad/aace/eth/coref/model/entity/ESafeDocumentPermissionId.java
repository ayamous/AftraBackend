package ma.itroad.aace.eth.coref.model.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ESafeDocumentPermissionId implements Serializable {

    @Column(name = "document_id")
    Long documentId;

    @Column(name = "user_account_id")
    Long userAccountId;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof ESafeDocumentPermissionId)) {
            return false;
        }
        ESafeDocumentPermissionId object = (ESafeDocumentPermissionId) obj;
        return object.documentId == this.documentId && object.userAccountId == this.userAccountId;
    }
}
