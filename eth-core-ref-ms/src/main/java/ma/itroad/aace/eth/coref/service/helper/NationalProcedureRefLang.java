package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.CustomsRegimConstraints;
import ma.itroad.aace.eth.coref.service.validator.OrganizationConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class NationalProcedureRefLang extends CodeEntity {

	@NotBlank
	@CountryConstraints
	private String countryRef;

	@NotBlank
	@CustomsRegimConstraints
	private String customsRegimRef;


	@NotBlank
	@OrganizationConstraints
	private String organization;
	/*
	trnslate
	 */

	@NotBlank
	private String label;
	@NotBlank
	private String generalDescription;
	@NotBlank
	@langConstraints
	private String lang;
}
