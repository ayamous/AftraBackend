package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.service.ICityRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CityRefMapper extends CommonMapper implements GenericModelMapper<CityRef, CityRefBean> {

    private final String methodName = this.getClass().getName()+ "";

    @Autowired
    ICityRefService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract CityRefBean entityToBean(CityRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {

        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }
}
