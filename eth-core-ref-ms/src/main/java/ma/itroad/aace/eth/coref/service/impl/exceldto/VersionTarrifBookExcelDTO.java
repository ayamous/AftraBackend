package ma.itroad.aace.eth.coref.service.impl.exceldto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VersionTarrifBookExcelDTO {
	private String tarifBookReference;
    private String versionRefReference;
    private String countryRefReference;
    private String label;
    private String description;
    private String lang;
}
