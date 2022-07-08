package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface TariffBookRefLangProjection {

    Long getId();

    Date getCreatedOn();
    Date getUpdatedOn();

    String getReference();
    String getHsCode();
    String getProductYear();
    String getChapterRef();
    String getParent();

    /**
     * jointure
     */
    String getUnitRef();


    /**
     * traduction
     */
    String getLabel();
    String getDescription();
    String getLang();




}
