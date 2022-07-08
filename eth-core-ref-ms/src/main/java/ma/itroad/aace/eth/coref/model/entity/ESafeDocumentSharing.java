package ma.itroad.aace.eth.coref.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.model.enums.ESafeDocumentSharingMode;

import javax.persistence.*;


@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "e_safe_document_sharing" , schema = "eth")
public class ESafeDocumentSharing extends AuditEntity {

    @ManyToOne
    @JoinColumn(name = "document_id")
    private ESafeDocument document;

    @ManyToOne
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    @Enumerated(EnumType.STRING)
    private ESafeDocumentSharingMode mode;

    private boolean enabled;
}
