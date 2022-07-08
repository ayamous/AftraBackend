package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.entity.CodeEntity;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;
import ma.itroad.aace.eth.coref.service.validator.TechBarrierConstraints;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechBarrierTariffBookVM extends CodeEntity implements Serializable {
    private Long techBarrierRefId;
    @TechBarrierConstraints
    private String techBarrierRefCode;
    private Long tarifBookId;
    @TarifBookConstraint
    private String tarifBookReference;
    private String tariffBookLabel;
    private String techBarrierRefLabel;
    private String tariffBookDescription;
    private String lang;
}
