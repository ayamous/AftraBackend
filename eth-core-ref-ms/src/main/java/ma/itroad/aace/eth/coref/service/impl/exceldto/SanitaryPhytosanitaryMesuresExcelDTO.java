package ma.itroad.aace.eth.coref.service.impl.exceldto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SanitaryPhytosanitaryMesuresExcelDTO {
	private String label;
	private String description;
	private String lang;
	private String code;
	private String referenceDoc;
	private String referenceCountry;
	private String organization;
}
