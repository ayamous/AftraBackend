package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.service.validator.CityRefConstraints;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Setter
@Getter
public class OrganizationHelper extends AuditEntityBean implements Serializable {
    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String reference;
    private String acronym;
    private String name;
    @CountryConstraints
    private String countryRef;
    private String parent;
    @CityRefConstraints
    private String cityRef;
    private String categoryRef;
    private String adresse;
    private String email;
    private String tel;
}
