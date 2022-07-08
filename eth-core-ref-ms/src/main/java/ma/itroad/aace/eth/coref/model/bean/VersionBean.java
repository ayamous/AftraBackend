package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.enums.StatusVersion;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class VersionBean extends AuditEntityBean implements Serializable {

    private String version;
    private boolean enabled;
    private StatusVersion status;
    private Date applicatedOn;
    private Date validatedOn;
    private Date archivedOn;

 }
