package ma.itroad.aace.eth.coref.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "e_safe_document_permission", schema = "eth")
public class ESafeDocumentPermission implements Serializable {

    @EmbeddedId
    ESafeDocumentPermissionId id;

    @ManyToOne
    @MapsId("documentId")
    @JoinColumn(name = "document_id")
    private ESafeDocument document;

    @ManyToOne
    @MapsId("userAccountId")
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    private boolean sharable;

    private boolean visible;

    private boolean enabled;

    @Transient
    public void createId(ESafeDocument eSafeDocument, UserAccount userAccount) {
        if (id == null) {
            id = new ESafeDocumentPermissionId();
        }
        id.setDocumentId(eSafeDocument.getId());
        id.setUserAccountId(userAccount.getId());
        document = eSafeDocument;
        this.userAccount = userAccount;
    }

    @Transient
    public void createId(ESafeDocumentPermissionId id, ESafeDocument eSafeDocument, UserAccount userAccount) {
        this.id = id;
        document = eSafeDocument;
        this.userAccount = userAccount;
    }
}
