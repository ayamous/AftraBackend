package ma.itroad.aace.eth.coref.service.helper;


import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.LongSummaryStatistics;

@Getter
@Setter
public class CityRefEntityRefLang extends AuditEntityBean {

    @NotBlank
    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String reference;
    // private CountryRefBean countryRef;

    @NotBlank
    @CountryConstraints
    private String countryRef;

    @NotBlank
    private String label;

    @NotBlank
    private String description;

    @NotBlank
    @langConstraints
    private String lang;
}
