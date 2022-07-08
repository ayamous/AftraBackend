package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface SanitaryPhytosanitaryMeasuresRefLangProjection {

    Long getId();

    Date getCreatedOn();
    Date getUpdatedOn();

    String getCreatedBy();
    String getUpdatedBy();

    String getGeneralDescription();
    String getRequestOrigin();
    String getOrganization();

    String getCountryRef();
    /**
     * traduction
     */
    String getLabel();
    String getDescription();
    String getLang();

    Long getVersion_nbr();
    Boolean getNew();

    String getCode();



}
