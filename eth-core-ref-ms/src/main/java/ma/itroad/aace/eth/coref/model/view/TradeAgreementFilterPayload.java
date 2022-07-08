package ma.itroad.aace.eth.coref.model.view;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TradeAgreementFilterPayload {
    private String code;
    private String countryRefCode;
    private String agreementType;
    private AgreementStatus agreementStatus;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfAgreement;
}
