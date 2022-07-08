package ma.itroad.aace.eth.coref.service.helper;

import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LangEntityRefLang extends CodeEntityBean {

	
	@NotBlank
    private String name;
	@NotBlank
    private String def;
	
    @NotBlank
    private String label;
    @NotBlank
    private String description;
    @NotBlank
    @langConstraints
    private String lang;
}
