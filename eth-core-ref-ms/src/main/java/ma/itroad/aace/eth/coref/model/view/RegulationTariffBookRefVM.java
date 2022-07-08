package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.service.validator.RegulationConstraints;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegulationTariffBookRefVM implements Serializable {

	@RegulationConstraints
    private String regulationReference;
	@TarifBookConstraint
    private String tarifBookReference;
    private Long regulationId;
    private Long tarifBookId;
    private String regulationLabel;
    private String regulationDescription;
    private String tarifBookLabel;
    private String tarifBookDescription;
    private String lang;

}
