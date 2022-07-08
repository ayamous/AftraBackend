/**
 * 
 */
package ma.itroad.aace.eth.coref.service.helper;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

/**
 *
 */
@Setter
@Getter
public class PortRefLang  extends CodeEntity {

	@CountryConstraints
	@NotBlank
	private String countryRef;

	/*
	translate
	 */
	@NotBlank
	private String label;
	@NotBlank
	private String description;
	@NotBlank
	@langConstraints
	private String lang;

}
