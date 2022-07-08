package ma.itroad.aace.eth.coref.service.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalBarrierRefLang extends CodeEntity{

	private String countryRef;
	private String customsRegimRef;
	private String organization;

	/*
	translate
	 */

	private String label;
	private String generalDescription;
	private String lang;
}
