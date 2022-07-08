package ma.itroad.aace.eth.coref.model.mapper.projections;

import ma.itroad.aace.eth.coref.model.enums.RegimType;

import java.util.Date;

public interface CustomsRegimRefEntityRefLangProjection {

    Long getId();
    Date getCreatedOn();
    Date getUpdatedOn();
    String getCode();

    String getLabel();
    String getDescription();
    String getLang();
    RegimType getRegimType();
}
