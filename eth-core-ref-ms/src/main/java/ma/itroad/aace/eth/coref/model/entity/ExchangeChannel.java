package ma.itroad.aace.eth.coref.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelMode;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ExchangeChannel extends AuditEntity {

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeChannelType type;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeChannelMode mode;
    private String host;
    private int port;
    private String username;
    private String protocol;
    private boolean tTLs;
    private boolean authRequired;
    private boolean tested;

    @Transient
    public boolean sameAs(ExchangeChannel other) {
        if (other == null)
            return false;
        return mode.equals(other.mode) && type.equals(other.type);
    }
}
