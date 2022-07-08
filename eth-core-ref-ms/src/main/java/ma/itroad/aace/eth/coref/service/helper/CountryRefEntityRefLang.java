package ma.itroad.aace.eth.coref.service.helper;


import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.AuditEntityBean;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class CountryRefEntityRefLang extends AuditEntityBean {
    @NotBlank
    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String codeIso;

    @NotBlank
    @Pattern(regexp = "^[ a-zA-Z0-9]*$")
    private String reference;

    @NotBlank
    private String label;

    @NotBlank
    private String description;

    @NotBlank
    @langConstraints
    private String lang;
}
