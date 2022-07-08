package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.enums.DocumentType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "eth.e_document_type_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(JsonInclude.Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "e_document_type" , schema = "eth")
public class EDocumentType  extends CodeEntity {

    @OneToMany
    Set<ESafeDocument> eSafeDocumentSet ;

    @Enumerated(EnumType.ORDINAL)
    private DocumentType docType;

}

