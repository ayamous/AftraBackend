package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.country_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "country_ref", schema = "eth")
public class CountryRef extends AuditEntity {
    @Column(nullable = false)
    private String codeIso;

    @Column(name = "reference", unique = true, nullable = false)
    private String reference;

    @OneToMany(mappedBy = "countryRef")
    private List<VersionTariffBookRef> versionTariffBooks;

    @OneToMany(mappedBy = "countryRef")
    private List<CityRef> cityRefs;

    @ManyToMany(mappedBy = "countryRefs")
    @JsonIgnore
    private List<CountryGroupRef> countryGroupRefs;

    @OneToMany(mappedBy = "countryRef")
    private List<CustomsOfficeRef> customsOfficeRefs;

    @OneToMany(mappedBy = "countryRef")
    private Set<NationalProcedureRef> nationalProcedureRef ;


    @OneToMany(mappedBy = "countryRef")
    private List<TaxRef> taxRefs;

    @OneToMany(mappedBy = "countryRef")
    private List<Taxation> taxations;

    @OneToMany(mappedBy = "countryRef")
    private Set<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs ;

    @OneToMany(mappedBy = "countryRef")
    private List<Organization> organizations;

    @OneToMany(mappedBy = "countryRef")
     private List<RegulationRef> regulationRefs;


}
