package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface NationalProcedureRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCountryRef();
    String getCustomsRegimRef();
    String getGeneralDescription();
    String getOrganization();
    String getLabel();
    String getDescription();
    String getLang();
    String getCode();
    Long getVersion_nbr();
    Boolean getNew();


}
