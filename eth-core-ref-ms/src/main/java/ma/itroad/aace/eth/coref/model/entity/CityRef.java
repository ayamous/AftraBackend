package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.city_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(JsonInclude.Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "city_ref", schema = "eth")
public class CityRef extends AuditEntity {

    @Column(nullable = false)
    private String reference;

    @OneToMany(mappedBy = "cityRef")
    private List<Organization> organizations;

    @ManyToOne
    @JoinColumn(name = "country_ref_id", nullable = false)
    private CountryRef countryRef;

}
