package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.model.enums.StatusVersion;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.version_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "version_ref", schema = "eth")
public class VersionRef extends AuditEntity {
    private static final long serialVersionUID = 3847205726858956918L;

    @Column(name = "version", unique = true)
    private String version;

    private boolean enabled;

    private StatusVersion statusVersion;

    @Column(name = "applicated_on")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate applicatedOn;


    @Column(name = "validated_on")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate validatedOn;

    @Column(name = "archived_on")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate archivedOn;

    @OneToMany(mappedBy = "versionRef", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<VersionTariffBookRef> versionTariffBooks;
}
