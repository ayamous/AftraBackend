package ma.itroad.aace.eth.core.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntityListener;
import ma.itroad.aace.eth.core.model.entity.BaseEntity;
import ma.itroad.aace.eth.core.model.enums.RequestOriginEnum;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AuditEntityBean extends BaseEntityBean {

    Long version_nbr;

    protected String createdBy;

    protected Date createdOn;

    protected String updatedBy;

    protected Date updatedOn;

    protected RequestOriginEnum requestOrigin;

    protected List<InternationalizationVM> internationalizationVMList;



}
