package ma.itroad.aace.eth.core.model.entity;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.enums.RequestOriginEnum;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(value = { AuditEntityListener.class })
public abstract class AuditEntity extends BaseEntity {
    private static final long serialVersionUID = -1775849166653256400L;

    // Conditional Operations with Headers
    // https://docs.spring.io/spring-data/rest/docs/current/reference/html/#conditional
    protected @Version Long version_nbr;

    
    @JoinColumn(name = "created_by")
    protected String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on", updatable = false)
    protected Date createdOn;

    @JoinColumn(name = "updated_by")
    protected String updatedBy;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_on")
    protected Date updatedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_origin")
    protected RequestOriginEnum requestOrigin;
}
