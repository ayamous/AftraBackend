package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface ChapterRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getSectionRef();
    String getCode();

    String getLabel();
    String getDescription();
    String getLang();
}
