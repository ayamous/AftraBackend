package ma.itroad.aace.eth.coref.service.impl.exceldto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomsOfficeRefExcelDTO {
	private String label;
	private String description;
	private String lang;
	protected String code;
	private String countryRef;
}
