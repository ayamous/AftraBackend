package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface TaxationRefLangProjection {

    Long getId();

    Date getCreatedOn();
    Date getUpdatedOn();

    String getCreatedBy();
    String getUpdatedBy();

    String getReference();
    String getRate();
    String getValue();
    String getGeneralDescription();

    /**
    * jointure
    */
    String getCountryRef();
    String getCustomsRegimRef();
    String getUnitRef();
    String getTaxRef();


    /**
    * traduction
    */
    String getLabel();
    String getDescription();
    String getLang();

    Long getVersion_nbr();
    Boolean getNew();

}
