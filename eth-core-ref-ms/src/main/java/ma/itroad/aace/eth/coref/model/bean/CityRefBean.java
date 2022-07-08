package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;

import java.util.List;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class CityRefBean extends AuditEntityBean {

    private String reference;
    private CountryGroupRefBean countryRef;
    private List<InternationalizationVM> internationalizationVMList;
}
