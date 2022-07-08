package ma.itroad.aace.eth.coref.service.helper.detailed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.UnitRefEntityRefLang;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TaxationRefLangDetailed extends AuditEntity {

	private String reference;
	private String rate;
	private String value;

	/**
	 * jointure
	 */

	private CountryRefEntityRefLang countryRef;
	private CustomsRegimRefEntityRefLang customsRegimRef;
	private UnitRefEntityRefLang unitRef;
	private String taxRef;

	/**
	 * traduction
	 */
	private String label;
	private String generalDescription;
	private String lang;
}
