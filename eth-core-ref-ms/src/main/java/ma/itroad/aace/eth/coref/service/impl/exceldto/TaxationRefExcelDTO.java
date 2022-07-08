package ma.itroad.aace.eth.coref.service.impl.exceldto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TaxationRefExcelDTO {
	private String label;
	private String description;
	private String lang;
	private String reference;
	private String impositionPercentage;
	private String impositionValue;
	private String countryId;
	private String regimeCode;
	private String codeUnite;
	private String taxTypeCode;
	private String rate;
	private String value;
}
