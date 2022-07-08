/**
 * 
 */
package ma.itroad.aace.eth.coref.service.helper;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

/**
 *
 */
@Setter
@Getter
@NoArgsConstructor
public class CustomsOfficeRefLang extends CodeEntityBean {
	
	@NotBlank
	private String label;
	@NotBlank
    private String description;
	@NotBlank
	@langConstraints
    private String lang;
	
    protected String code;
    
    @CountryConstraints
    @NotBlank
    private String countryRef;
}
