package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.enums.DocumentType;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.e_safe_document_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(JsonInclude.Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "e_safe_document" , schema = "eth")
public class ESafeDocument extends AuditEntity {

    private Long fileSize;
    private String fileName;
    private String url;
    private Boolean isFolder;
    private Boolean isStarred;
    private Boolean isSharedDoc;
    private Boolean isSharedFolder;
    private LocalDate expirationDate;
    private Long edmStorld;
    private Long version;
    private Boolean isArchived;
    private String title;
    private String auteur;

    @Column(unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    private ExchangeChannelType channelType;

    @ManyToOne
    private EDocumentType documentType ;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    @OneToMany(mappedBy = "document", cascade = CascadeType.REMOVE)
    private Set<ESafeDocumentSharing> shares;

    @OneToMany(mappedBy = "document", cascade = CascadeType.REMOVE)
    private Set<ESafeDocumentPermission> permissions;

    @ManyToOne
    @JoinColumn(name="economicOperator_id")
    private EconomicOperator economicOperator;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ESafeDocument parent;

    @OneToMany( cascade = CascadeType.REMOVE , mappedBy = "parent")
    private Set<ESafeDocument> children;

    @ManyToOne
    @JoinColumn(name="organization_id")
    private Organization organization;



}
