package ma.itroad.aace.eth.core.model.entity;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

public class AuditEntityListener {

    @PrePersist
    public void prePersist(AuditEntity item) {
        if (item != null) {
            item.setCreatedOn(new Date());
        }
    }

    @PreUpdate
    public void preUpdate(AuditEntity item) {
        if (item != null) {
            item.setUpdatedOn(new Date());

        }
    }

}
