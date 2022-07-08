package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;

import java.io.Serializable;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationBean extends AuditEntityBean implements Serializable {
    private String reference;
    private String acronym;
    private String name;
    private CountryRefBean countryRef;
    private OrganizationBean parent;
    private CityRefBean cityRef;
    private CategoryRefBean categoryRef;
    private String adresse;
    private String email;
    private String tel;
}
