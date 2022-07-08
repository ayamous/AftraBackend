package ma.itroad.aace.eth.coref.model.view;

import lombok.*;
import ma.itroad.aace.eth.coref.service.validator.AgreementConstraints;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TarifBookAgreementVM implements Serializable {
    private Long tarifBookId;
    private Long agreementId;
    @TarifBookConstraint
    private String tarifBookReference;
    @AgreementConstraints
    private String agreementReference;
    private String tariffBookLabel;
    private String tariffBookDescription;
    private String agreementLabel;
    private String agreementDescription;
    private String lang;

    public TarifBookAgreementVM (Long tarifBookId, Long agreementId , String tarifBookReference,String agreementReference){
        this.tarifBookId=tarifBookId ;
        this.agreementId=agreementId;
        this.tarifBookReference=tarifBookReference;
        this.agreementReference=agreementReference ;
    }
}
