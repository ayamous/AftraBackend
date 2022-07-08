package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class AuthorizationSetupBean implements Serializable {
    private Long defaultPrice;
    private Long defaultVAT;
    private Integer defaultValidityDays;
    private Integer validityManadatory;
    private AuthorizationSetupBean PredecessorAuthorization;
    private OrganizationBean organizationBean;
    List<InternationalizationVM> internationalizationVMList;
}
