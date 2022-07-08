package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.List;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.lang_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lang" , schema = "eth")
public class Lang extends CodeEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String def;

}
