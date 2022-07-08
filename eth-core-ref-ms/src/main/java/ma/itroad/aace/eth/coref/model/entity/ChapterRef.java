package ma.itroad.aace.eth.coref.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;

@GenericGenerator(name = "eth_seq_generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = { @Parameter(name = "sequence_name", value = "eth.chapter_ref_seq") })

//-- SectionRef Default View ---------------------------------------------------------------------------------
/**/ @JsonInclude(Include.NON_NULL)
//----------------------------------------------------------------------------------------------------------

@Getter
@Setter
@Entity
@Table(name = "chapter_ref" , schema = "eth")
public class ChapterRef extends CodeEntity {
    private static final long serialVersionUID = 3847205726858956918L;

    @ManyToOne
    @JoinColumn(name = "section_ref_id", nullable = true)
    private SectionRef sectionRef;

    @Transient
    private String label;
    
    @Transient
    private String description;
    
    @Transient
    private String lang;

}
