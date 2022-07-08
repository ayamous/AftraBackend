package ma.itroad.aace.eth.coref.service.helper.detailed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalBarrierRefLangDetailed extends CodeEntity{

	private CountryRefEntityRefLang countryRef;
	private CustomsRegimRefEntityRefLang customsRegimRef;
	private String organization;

	/*
	translate
	 */

	private String label;
	private String generalDescription;
	private String lang;
}
