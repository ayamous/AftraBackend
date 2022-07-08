package ma.itroad.aace.eth.coref.model.mapper.projections;

import com.fasterxml.jackson.annotation.JsonFormat;
import ma.itroad.aace.eth.coref.model.enums.StatusVersion;

import java.time.LocalDate;
import java.util.Date;

public interface VersionRefEntityRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();

    String getLabel();
    String getDescription();
    String getLang();

     String getVersion();
     boolean getEnabled();
     StatusVersion getStatusVersion();

    @JsonFormat(pattern = "yyyy-MM-dd")
     LocalDate getApplicatedOn();

    @JsonFormat(pattern = "yyyy-MM-dd")
     LocalDate getValidatedOn();

    @JsonFormat(pattern = "yyyy-MM-dd")
     LocalDate getArchivedOn();



}
