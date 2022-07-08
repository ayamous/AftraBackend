package ma.itroad.aace.eth.coref.service.helper;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;
import ma.itroad.aace.eth.coref.service.validator.CountryConstraints;
import ma.itroad.aace.eth.coref.service.validator.langConstraints;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgreementLang extends CodeEntityBean {

    private String title;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfAgreement;
    private AgreementStatus agreementStatus;
    private String description;


    /*
    * relationTable
    */
    private String agreementType;
    private String countryGroupRef;
    @CountryConstraints
    private String countryRef;

    /**
     * Translation
     */
    @NotBlank
    private String label;
    @NotBlank
    private String generalDescription;
    @NotBlank
    @langConstraints
    private String lang;
}