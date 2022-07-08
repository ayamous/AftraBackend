package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class NationalProcedureRefBean extends CodeEntityBean implements Serializable {
    private CountryRefBean countryRef;
    private CustomsRegimRefBean customsRegimRef ;
    private OrganizationBean organization ;
    private ESafeDocumentBean eSafeDocument ;

    private String label;
    private String generalDescription;
    private String lang;
}
