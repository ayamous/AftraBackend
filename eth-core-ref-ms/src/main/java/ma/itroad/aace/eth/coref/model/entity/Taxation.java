package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.taxation_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "taxation", schema = "eth")
public class Taxation extends AuditEntity {

    @Column(nullable = false, unique = true)
    private String reference;

    private String rate;

    private String value;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CountryRef countryRef;

    @ManyToOne
    @JoinColumn(name = "customs_regim_id")
    private CustomsRegimRef customsRegimRef;

    @ManyToOne
    @JoinColumn(name = "unit_ref_id")
    private UnitRef unitRef;

    @ManyToOne
    @JoinColumn(name = "tax_ref_id")
    private TaxRef taxRef;

    @ManyToMany
    @JoinTable(name = "TARIF_BOOK_REF_TAXATIONS_REF_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "TAXATIONS_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_TAXATIONS_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "FK_TARIF_BOOK_REF_ID", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_TARIF_BOOK_REF_ID"))
    )
    private Set<TarifBookRef> tarifBookRefs;
    
    @Transient
    private String label;
    
    @Transient
    private String generalDescription;
    
    @Transient
    private String lang;


    @OneToOne
    private ESafeDocument eSafeDocument ;

}
