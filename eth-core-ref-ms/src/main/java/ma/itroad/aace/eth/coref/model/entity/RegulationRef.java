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
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.regulation_ref_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "regulation_ref" , schema = "eth")
public class RegulationRef extends CodeEntity {
    @ManyToOne
    @JoinColumn(name = "country_ref_id", nullable = true)
    private CountryRef countryRef;

    @ManyToMany(mappedBy = "regulationRef")
    private Set<CustomsRegimRef> customsRegimRefs;

    @ManyToMany
    @JoinTable(name = "REGULATION_TARIFF_BOOK_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "regulation_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_regulation_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "tarif_book_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_tarif_book_REF_ID")))
    private Set<TarifBookRef> tarifBookRefs;
    @ManyToOne
    private Organization organization;

    @OneToOne
    private ESafeDocument eSafeDocument ;

    @Transient
    private String label;

    @Transient
    private String generalDescription;

    @Transient
    private String lang;
/*
    @Column(columnDefinition="TEXT")
    private String generalDescription;

 */
}
