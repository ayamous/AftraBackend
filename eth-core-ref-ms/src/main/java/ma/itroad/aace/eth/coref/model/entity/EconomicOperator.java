package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.aspectj.weaver.ast.Or;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.economic_operator_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "economic_operator" , schema = "eth")
public class EconomicOperator extends CodeEntity {
    private String legalForm;
    private Long tradeRegisterNumber;
    private Long agreementNumber;
    private String taxIdentifierNumber;
    private String importer;
    private String exporter;
    private String clearingAgent;
    @OneToMany(mappedBy="economicOperator")
    private Set<ESafeDocument> ESafeDocument;

    @ManyToOne
    private Organization organization ;
}
