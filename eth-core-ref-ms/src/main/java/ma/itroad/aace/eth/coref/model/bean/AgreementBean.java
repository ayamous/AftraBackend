package ma.itroad.aace.eth.coref.model.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class AgreementBean extends CodeEntityBean implements Serializable {
    private  String title;
    private  String description;
    private LocalDate dateOfAgreement ;
    private AgreementStatus agreementStatus;
    private AgreementTypeBean agreementType ;
    private  CountryGroupRefBean countryGroupRef;
   // private  CountryRefBean countryRef;
    private  CountryRefBean countryCode;
   private ESafeDocumentBean eSafeDocument ;

    private String label;
    private String generalDescription;
    private String lang;
}
