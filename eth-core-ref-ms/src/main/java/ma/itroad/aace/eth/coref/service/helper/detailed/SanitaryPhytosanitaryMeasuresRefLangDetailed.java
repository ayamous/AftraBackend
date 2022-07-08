package ma.itroad.aace.eth.coref.service.helper.detailed;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;

@Setter
@Getter
public class SanitaryPhytosanitaryMeasuresRefLangDetailed extends CodeEntity {

	private CountryRefEntityRefLang countryRef;


	private String label;
    private String generalDescription;
    private String lang;
}
