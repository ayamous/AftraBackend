package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface UnitRefEntityRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getName();
    String getCode();

    String getLabel();
    String getDescription();
    String getLang();

}
