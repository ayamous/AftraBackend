package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.version_tariff_book_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "version_tariff_book_ref", schema = "eth")
public class VersionTariffBookRef extends AuditEntity {
    private static final long serialVersionUID = 3847205726858956918L;

    @ManyToOne
    @JoinColumn(name = "tariff_book_ref_id", nullable = true)
    private TarifBookRef tarifBookRef;

    @ManyToOne
    @JoinColumn(name = "version_ref_id", nullable = true)
    private VersionRef versionRef;

    @ManyToOne
    @JoinColumn(name = "country_ref_id", nullable = true)
    private CountryRef countryRef;
    
    @Transient
    private String label;
    
    @Transient
    private String description;
    
    @Transient
    private String lang;


}
