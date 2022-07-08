package ma.itroad.aace.eth.coref.service.helper;


import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
public class CountryGroupRefsEntityRefLang  extends CodeEntityBean {

    @NotBlank
    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String reference;

    @NotBlank
    private String label;

    @NotBlank
    private String description;
    //private Long lang;

    @NotBlank
    @langConstraints
    private String lang;
}

