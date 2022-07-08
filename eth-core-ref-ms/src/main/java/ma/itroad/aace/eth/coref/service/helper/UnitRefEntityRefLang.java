package ma.itroad.aace.eth.coref.service.helper;


import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

@Getter
@Setter
public class UnitRefEntityRefLang  extends CodeEntityBean {

	@NotBlank
    private String name;

    /**
     * Translation
     */
	@NotBlank
    private String label;
	@NotBlank
    private String description;
	@NotBlank
	@langConstraints
    private String lang;
}
