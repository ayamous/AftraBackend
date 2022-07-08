package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface LangEntityRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();

    String getDef();
    String getName();

    String getLabel();
    String getDescription();
    String getLang();
}
