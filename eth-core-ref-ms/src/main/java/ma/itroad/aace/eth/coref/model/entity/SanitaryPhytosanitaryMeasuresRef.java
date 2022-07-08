package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.sanitary_phytosanitary_measures_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "sanitary_phytosanitary_measures_ref", schema = "eth")
public class SanitaryPhytosanitaryMeasuresRef extends CodeEntity {

    @ManyToOne
    private CountryRef countryRef;

   @ManyToMany(mappedBy = "sanitaryPhytosanitaryMeasuresRef")
    private Set<CustomsRegimRef> customsRegimRefs;

    @ManyToMany
    @JoinTable(name = "MSP_TARIFF_BOOK_JOIN", schema = "eth",
            joinColumns = @JoinColumn(name = "sanitary_phytosanitary_measures_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_sanitary_phytosanitary_measures_REF_ID")),
            inverseJoinColumns = @JoinColumn(name = "tarif_book_REF_ID", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_tarif_book_REF_ID")))
    private Set<TarifBookRef> tarifBookRefs;

    @ManyToOne
    private Organization organization ;

    @OneToOne
    private ESafeDocument eSafeDocument ;
/*
    @Column(columnDefinition="TEXT")
    private String generalDescription ;
*/
    @Transient
    private String label;

    @Transient
    private String generalDescription;

    @Transient
    private String lang;

}
