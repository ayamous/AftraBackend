package ma.itroad.aace.eth.coref.service.impl.exceldto;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;

@Setter
@Getter
public class PortRefExcelDTO {
	private String label;
	private String description;
	private String lang;
	protected String code;
	private String countryRef;
}
