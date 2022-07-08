package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TechBarrierRefBean  extends CodeEntityBean implements Serializable {
    private CountryRefBean countryRef;
    private CustomsRegimRefBean customsRegimRef ;
    private OrganizationBean organization ;
    private ESafeDocumentBean eSafeDocument ;

	private String label;
	private String generalDescription;
	private String lang;
}
