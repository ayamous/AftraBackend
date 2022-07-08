package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;

import java.io.Serializable;
import java.util.List;


@Setter
@JsonInclude(Include.NON_NULL)
public class EconomicOperatorBean implements Serializable {
    private String legalForm;
    private Long tradeRegisterNumber;
    private Long agreementNumber;
    private String taxIdentifierNumber;
    private String importer;
    private String exporter;
    private String clearingAgent;
    private List<InternationalizationVM> internationalizationVMList;
}
