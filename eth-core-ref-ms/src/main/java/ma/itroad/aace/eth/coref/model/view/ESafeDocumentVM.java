package ma.itroad.aace.eth.coref.model.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentPermissionRequest;
import ma.itroad.aace.eth.coref.model.enums.DocumentType;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ESafeDocumentVM extends AuditEntityBean implements Serializable {
    private Long fileSize;
    private String fileName;
    private Boolean isFolder;
    private Boolean isStarred;
    private Boolean isSharedDoc;
    private Boolean isSharedFolder;
    private Boolean isArchived;
    private LocalDate expirationDate;
    private Long edmStorld;
    private Long version;

    private String docType;
    private Long idEntity ;

    private String economicOperatorCode ;
    private Long parentEdmStorld ;
    private String documentSetupTechReference ;
    private String organizationReference ;

    private boolean visibleToAll;
    private boolean sharedWithAll;
    private ESafeDocumentPermissionRequest sharingPermission;
    private ESafeDocumentPermissionRequest visibilityPermission;
    private String title;
    private String auteur;
    private String reference;
    private ExchangeChannelType channelType;
}

