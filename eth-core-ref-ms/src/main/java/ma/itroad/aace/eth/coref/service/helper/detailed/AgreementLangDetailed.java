package ma.itroad.aace.eth.coref.service.helper.detailed;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.CodeEntityBean;
import ma.itroad.aace.eth.coref.model.entity.AgreementType;
import ma.itroad.aace.eth.coref.model.entity.CountryGroupRef;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;
import ma.itroad.aace.eth.coref.repository.CountryRefRepository;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupRefsEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgreementLangDetailed extends CodeEntityBean {

    private String title;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfAgreement;
    private AgreementStatus agreementStatus;
    private String description;

    /*
    * relationTable
    */
    private AgreementType agreementType;
    private CountryGroupRefsEntityRefLang countryGroupRef;
    private CountryRefEntityRefLang countryRef;
    /**
     * Translation
     */
    private String label;
    private String generalDescription;
    private String lang;
}