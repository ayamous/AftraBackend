package ma.itroad.aace.eth.coref.model.mapper.projections;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import ma.itroad.aace.eth.coref.model.entity.AgreementType;
import ma.itroad.aace.eth.coref.model.entity.CountryGroupRef;
import ma.itroad.aace.eth.coref.model.enums.AgreementStatus;

import java.time.LocalDate;
import java.util.Date;

public interface AgreementLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();

    String getTitle();

    LocalDate getDateOfAgreement();
    AgreementStatus getAgreementStatus();
    String getDescription();
    String getGeneralDescription() ;

    String getAgreementType();
    String getCountryGroupRef();
    String getCountryRef();


    String getLabel();
    String getLangDescription();
    String getLang();


    String getCode();
    Long getVersion_nbr();


}
