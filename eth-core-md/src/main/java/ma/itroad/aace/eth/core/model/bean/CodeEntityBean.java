package ma.itroad.aace.eth.core.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class CodeEntityBean extends AuditEntityBean {
    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String code;
    List<InternationalizationVM> internationalizationVMList;
   }


