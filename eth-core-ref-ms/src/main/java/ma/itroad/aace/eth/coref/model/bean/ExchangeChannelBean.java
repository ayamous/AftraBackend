package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.entity.ExchangeChannel;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelMode;
import ma.itroad.aace.eth.coref.model.enums.ExchangeChannelType;

import javax.persistence.*;
import java.io.Serializable;


@Data
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeChannelBean extends AuditEntityBean implements Serializable {
    private Long organizationId;
    private ExchangeChannelType type;
    private ExchangeChannelMode mode;
    private String host;
    private int port;
    private String username;
    private String protocol;
    private boolean tTLs;
    private boolean authRequired;
    private boolean tested;

    public static ExchangeChannelBean of(ExchangeChannel entity) {
        ExchangeChannelBean bean = ExchangeChannelBean.builder()
                .authRequired(entity.isAuthRequired())
                .tTLs(entity.isTTLs())
                .host(entity.getHost())
                .protocol(entity.getProtocol())
                .port(entity.getPort())
                .mode(entity.getMode())
                .type(entity.getType())
                .username(entity.getUsername())
                .tested(entity.isTested())
                .authRequired(entity.isAuthRequired())
                .organizationId(entity.getOrganization() == null ? null : entity.getOrganization().getId()).build();
        bean.setId(entity.getId());
        return bean;
    }
}
