package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.entity_ref_lang_seq")})

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(JsonInclude.Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "entity_ref_lang", schema = "eth")
public class EntityRefLang extends AuditEntity {

    @Column(columnDefinition="TEXT")
    private String label;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(columnDefinition="TEXT")
    private String generalDescription ;

    @ManyToOne()
    @JoinColumn(name = "lang_id")
    private Lang lang;

    @Enumerated(EnumType.STRING)
    private TableRef tableRef;

    @Column(name = "ref_id")
    private Long refId;

}
