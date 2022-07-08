package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.model.entity.EDocumentType;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.entity.EconomicOperator;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ESafeDocumentBean extends CodeEntityBean implements Serializable {

    private String fileName;
    private Boolean isFolder;
    private Boolean isStarred;
    private Boolean isSharedDoc;
    private Boolean isSharedFolder;
    private Date expirationDate;
    private Integer fileSize;
    private Long edmStorld;
    private Long version;
    private Boolean isArchived;
    private String docType;
    private ESafeDocumentBean parent;
    private OrganizationBean organization;
    private boolean isSharable;
    private String title;
    private String auteur;
    private String reference;

    private EDocumentType documentType ;

}
