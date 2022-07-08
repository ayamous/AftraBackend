package ma.itroad.aace.eth.core.model.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.proxy.HibernateProxy;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.helper.PersistenceUtilsHelper;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = -7548125117869613136L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "eth_seq_generator")
    protected Long id;

    @Transient
    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        Object thisObj = (this instanceof HibernateProxy) ? PersistenceUtilsHelper.unProxifyEntity(this) : this;
        Object otherObj = (obj instanceof HibernateProxy) ? PersistenceUtilsHelper.unProxifyEntity(obj) : obj;

        if (this.id == null || obj == null || !(thisObj.getClass().equals(otherObj.getClass()))) {
            return false;
        }

        return this.id.equals(((BaseEntity) obj).getId());
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName() + " (id: " + id + ")";
    }


}
