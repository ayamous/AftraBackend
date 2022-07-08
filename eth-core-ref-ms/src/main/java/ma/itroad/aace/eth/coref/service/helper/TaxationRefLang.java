package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
public class TaxationRefLang extends AuditEntity {

	@Pattern(regexp = "^[ a-zA-Z0-9]*$")
	private String reference;
	private String rate;
	private String value;

	/**
	 * jointure
	 */

	private String countryRef;
	private String customsRegimRef;
	private String unitRef;
	private String taxRef;

	/**
	 * traduction
	 */
	@NotBlank
	private String label;
	@NotBlank
	private String generalDescription;
	@NotBlank
	@langConstraints
	private String lang;

}
