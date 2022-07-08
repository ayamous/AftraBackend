package ma.itroad.aace.eth.coref.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.service.validator.ProcedureConstraints;
import ma.itroad.aace.eth.coref.service.validator.TarifBookConstraint;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TarifBookNationalProcedureVM implements Serializable {

    private Long tarifBookId;
    private Long nationalProcedureRefId;
    @TarifBookConstraint
    private String tarifBookReference;
    @ProcedureConstraints
    private String nationalProcedureCode;
    private String tariffBookLabel;
    private String tariffBookDescription;
    private String nationalProcedureLabel;
    private String nationalProcedureDescription;
    private String lang;

    public TarifBookNationalProcedureVM (Long tarifBookId, Long nationalProcedureRefId , String tarifBookReference,String nationalProcedureCode){
        this.tarifBookId=tarifBookId ;
        this.nationalProcedureRefId=nationalProcedureRefId;
        this.tarifBookReference=tarifBookReference;
        this.nationalProcedureCode=nationalProcedureCode ;
    }
}
