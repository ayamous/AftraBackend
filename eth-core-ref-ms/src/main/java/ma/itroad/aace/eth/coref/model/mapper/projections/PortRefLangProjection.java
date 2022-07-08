package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface PortRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();
    String getCountryRef();
    String getLabel();
    String getDescription();
    String getLang();
}
