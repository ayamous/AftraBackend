package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TaxationBean extends AuditEntityBean implements Serializable {

    private String reference;
    private String rate;
    private String value;
    private CountryRefBean countryRef ;
    private CustomsRegimRefBean customsRegimRef ;
    private UnitRefBean unitRef ;
    private TaxRefBean taxRef ;
    private ESafeDocumentBean eSafeDocument ;
    private String label;
    private String generalDescription;
    private String lang;

}
