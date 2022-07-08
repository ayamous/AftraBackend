package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface RegulationRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();

     String getCountryRef();
     String getOrganization();
     String getGeneralDescription();

    String getLabel();
    String getDescription();
    String getLang();
}
