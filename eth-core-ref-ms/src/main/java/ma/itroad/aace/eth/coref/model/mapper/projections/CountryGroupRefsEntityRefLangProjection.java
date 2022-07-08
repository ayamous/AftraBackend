package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface CountryGroupRefsEntityRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();
    String getReference();

    String getLabel();
    String getDescription();
    String getLang();
}
