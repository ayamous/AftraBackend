package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.model.bean.TariffBookRefBean;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.model.entity.TarifBookRef;
import ma.itroad.aace.eth.coref.service.ICityRefService;
import ma.itroad.aace.eth.coref.service.ITarifBookRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TariffBookRefMapper extends CommonMapper implements GenericModelMapper<TarifBookRef,TariffBookRefBean > {

    /*
    default TarifBookRef map(Long id) {
        if (id == null)    return null;
        TarifBookRef tarifBookRef = new TarifBookRef();
        tarifBookRef.setId(id);
        return tarifBookRef;
    }*/

    private final String methodName = this.getClass().getName()+ "";

    @Autowired
    ITarifBookRefService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract TariffBookRefBean entityToBean(TarifBookRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {

        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }
}