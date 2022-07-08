package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.authorization_setup_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "authorization_setup" , schema = "eth")
public class AuthorizationSetup extends AuditEntity {
    private Long defaultPrice;
    private Long defaultVAT;
    private Integer defaultValidityDays;
    private Integer validityManadatory;
    @ManyToOne
    @JoinColumn(name = "idPredecessorAuthorization")
    private AuthorizationSetup PredecessorAuthorization;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;




}
