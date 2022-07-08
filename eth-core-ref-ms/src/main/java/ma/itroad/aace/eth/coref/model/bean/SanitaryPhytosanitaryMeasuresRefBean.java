package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class SanitaryPhytosanitaryMeasuresRefBean extends CodeEntityBean implements Serializable {
    private CountryRefBean countryRef ;
    private OrganizationBean organization ;
    private ESafeDocumentBean eSafeDocument ;

    private String label;
    private String generalDescription;
    private String lang;
}
