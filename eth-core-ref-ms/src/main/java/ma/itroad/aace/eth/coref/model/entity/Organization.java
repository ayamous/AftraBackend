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
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.organization_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "organization", schema = "eth")
public class Organization extends AuditEntity {
    private String adresse;
    private String email;
    private String tel;
    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String acronym;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Organization parent;

    @OneToMany(mappedBy = "parent"/*, cascade = {CascadeType.ALL}*/)
    private List<Organization> childOrganisations;

    @ManyToOne
    @JoinColumn(name = "country_ref_id", foreignKey = @ForeignKey(name = "FK_ORGANIZATION_COUNTRY_REF_ID"))
    private CountryRef countryRef;

    @ManyToOne
    @JoinColumn(name = "city_ref_id", foreignKey = @ForeignKey(name = "FK_ORGANIZATION_CITY_REF_ID"))
    private CityRef cityRef;

    @ManyToOne
    private CategoryRef categoryRef;

    @OneToMany(mappedBy = "organization")
    private Set<ESafeDocument> eSafeDocuments;

    @OneToMany(mappedBy = "organization")
    private Set<UserAccount> userAccounts;

    @OneToMany(mappedBy = "organization")
    private Set<TechBarrierRef> techBarrierRefs;

    @OneToMany(mappedBy = "organization")
    private Set<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs;

    @OneToMany(mappedBy = "organization")
    private Set<EconomicOperator> economicOperators;

    @OneToMany(mappedBy = "organization")
    private Set<ExchangeChannel> exchangeChannels;

    public Organization reference(String reference) {
        this.setReference(reference);
        return this;
    }

}
