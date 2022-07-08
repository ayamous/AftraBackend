package ma.itroad.aace.eth.coref.model.mapper.projections;

import java.util.Date;

public interface CityRefEntityRefLangProjection {
     Long getId();
     Date getCreatedOn();
     Date getUpdatedOn();
     String getReference();
     String getCountryRef();
     String getLabel();
     String getDescription();
     String getLang();


}
