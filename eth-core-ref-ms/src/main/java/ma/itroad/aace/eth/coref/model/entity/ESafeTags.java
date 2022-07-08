package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.Table;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.esafe_tags_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "e_safe_tags" , schema = "eth")
public class ESafeTags extends AuditEntity {
    private static final long serialVersionUID = 3847205726858956918L;

    private String name;
}
