package ma.itroad.aace.eth.coref.service.helper;


import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class RefCurrencyRefEntityRefLang extends CodeEntityBean {

    @NotBlank
    private String label;

    @NotBlank
    private String description;

    //private Long lang;
    @NotBlank
    @langConstraints
    private String lang;

}
