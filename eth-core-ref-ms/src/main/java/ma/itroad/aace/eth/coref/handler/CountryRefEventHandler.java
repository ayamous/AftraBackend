package ma.itroad.aace.eth.coref.handler;

import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

@RepositoryEventHandler()
public class CountryRefEventHandler {

    @Autowired
    EntityRefLangRepository entityRefLangRepository;

    @HandleAfterCreate
    public void handleCountryRefAfterCreate(CountryRef countryRef) {
        // code for after create CountryRef event
        /*
        EntityRefLang entity = new EntityRefLang();
        entity.setDescription("desc");
        entity.setClazz(CountryRef.class.toString());
        entity.setLabel("label");
        entity.setIdTable(countryRef.getId());
        entityRefLangRepository.save(entity);
        */
    }
}
