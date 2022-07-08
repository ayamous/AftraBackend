package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfilBean extends AuditEntityBean implements Serializable {

    private String reference;

    private String name;

    private boolean enabled;

    private String description;
}
