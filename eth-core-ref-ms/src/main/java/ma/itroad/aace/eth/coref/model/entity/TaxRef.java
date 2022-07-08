package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.tax_ref_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "tax_ref", schema = "eth")
public class TaxRef extends CodeEntity {

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = true)
    private CountryRef countryRef;

    @OneToMany(mappedBy = "taxRef")
    private List<Taxation> taxations;
    
    @Transient
    private String label;
    
    @Transient
    private String description;
    
    @Transient
    private String lang;


}
