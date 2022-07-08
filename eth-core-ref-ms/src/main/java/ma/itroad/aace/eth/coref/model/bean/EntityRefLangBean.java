package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.core.model.enums.TableRef;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityRefLangBean extends AuditEntityBean implements Serializable {

    private String label;
    private String description;
    private Long langId;
    @Enumerated(EnumType.STRING)
    private TableRef tableRef;
    private Long refId;
}
