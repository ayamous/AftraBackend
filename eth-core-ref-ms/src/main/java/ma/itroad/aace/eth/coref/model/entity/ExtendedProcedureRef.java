package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.List;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.extended_procedure_ref_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/
@JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "extended_procedure_ref", schema = "eth")
public class ExtendedProcedureRef extends CodeEntity {
	@ManyToOne
	private NationalProcedureRef nationalProcedureRef;

	@Transient
	private String label;

	@Transient
	private String description;

	@Transient
	private String lang;

}
