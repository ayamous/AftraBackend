package ma.itroad.aace.eth.coref.service.helper;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeclarationTypeRefEntityRefLang extends CodeEntityBean {

    @NotBlank
    private String label;

    @NotBlank
    private String description;

    @NotBlank
    @langConstraints
    private String lang;
}
