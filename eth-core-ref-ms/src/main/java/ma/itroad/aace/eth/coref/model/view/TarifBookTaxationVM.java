package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;
import ma.itroad.aace.eth.coref.service.validator.TaxConstraints;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TarifBookTaxationVM implements Serializable {

    private Long tarifBookId;
    private Long taxationId;
    @TarifBookConstraint
    private String tarifBookReference;
    @TaxConstraints
    private String taxationReference;
    private String tariffBookLabel;
    private String tariffBookDescription;
    private String taxationLabel;
    private String taxationDescription;
    private String lang;

}
