package ma.itroad.aace.eth.coref.service.helper.detailed;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;

@Setter
@Getter
public class NationalProcedureRefLangDetailed extends CodeEntity {

	private CountryRefEntityRefLang countryRef;
	private CustomsRegimRefEntityRefLang customsRegimRef;
	/*
	trnslate
	 */

	private String label;
	private String generalDescription;
	private String lang;
}
