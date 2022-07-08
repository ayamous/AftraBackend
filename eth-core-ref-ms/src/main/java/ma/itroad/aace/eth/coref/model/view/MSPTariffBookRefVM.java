package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.service.validator.MspConstraints;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MSPTariffBookRefVM implements Serializable {

	@MspConstraints
	private String mspReference;
	@TarifBookConstraint
	private String tarifBookReference;
	private Long mspId;
	private Long tarifBookId;
	private String mspLabel;
	private String mspDescription;
	private String tarifBookLabel;
	private String tarifBookDescription;
	private String lang;

}
