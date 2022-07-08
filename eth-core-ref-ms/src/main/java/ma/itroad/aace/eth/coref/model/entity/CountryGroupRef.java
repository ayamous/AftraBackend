package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.country_group_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "country_group_ref", schema = "eth")
public class CountryGroupRef extends CodeEntity {

    @Column(name = "reference", length = 128, nullable = false, unique = true)
    private String reference;

    @ManyToMany
    @JoinTable(name = "country_group_join_ref", schema = "eth",
            joinColumns = @JoinColumn(name = "country_group_ref_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_countryRefs_ID")),
            inverseJoinColumns = @JoinColumn(name = "country_ref_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "FK_country_group_ref_ID")))
    private Set<CountryRef> countryRefs;

    @OneToMany(mappedBy = "countryGroupRef")
    private Set <Agreement> agreements;

}
