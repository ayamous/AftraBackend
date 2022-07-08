package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.TechBarrierRefBean;
import ma.itroad.aace.eth.coref.model.entity.TechBarrierRef;
import ma.itroad.aace.eth.coref.service.ITechBarrierRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Mapper(componentModel = "spring")
public abstract class TechBarrierRefMapper extends CommonMapper implements GenericModelMapper<TechBarrierRef, TechBarrierRefBean> {

    @Autowired
    ITechBarrierRefService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract TechBarrierRefBean entityToBean(TechBarrierRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}
