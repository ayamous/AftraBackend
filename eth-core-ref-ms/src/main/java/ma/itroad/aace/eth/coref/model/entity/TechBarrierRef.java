package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.tech_barrier_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "tech_barrier_ref", schema = "eth")

public class TechBarrierRef extends CodeEntity {
    @ManyToOne
    @JoinColumn(name = "country_ref_id", nullable = true)
    private CountryRef countryRef;

    @ManyToOne
    @JoinColumn(name = "custom_regim_ref_id", nullable = true)
    private CustomsRegimRef customsRegimRef;

    @ManyToMany
    @JoinTable(name = "TECH_BARRIER_REF_TARIFF_BOOK_REF_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "TECH_BARRIER_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_TECH_BARRIER_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "TARIFF_BOOK_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_TARIFF_BOOK_REF_ID")))
    private Set<TarifBookRef> tariffBookRefs;

    @Transient
    private String label;

    @Transient
    private String generalDescription ;
    
    @Transient
    private String lang;


    @ManyToOne
    @JoinColumn(name="organization_id")
    private Organization organization ;

    @OneToOne
    private ESafeDocument eSafeDocument ;


}
