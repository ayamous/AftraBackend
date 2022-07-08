package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface CategoryRefEntityRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();

    String getLabel();
    String getDescription();
    String getLang();


}
