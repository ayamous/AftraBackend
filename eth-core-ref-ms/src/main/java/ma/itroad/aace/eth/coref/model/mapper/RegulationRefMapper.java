package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.PortRefBean;
import ma.itroad.aace.eth.coref.model.bean.RegulationRefBean;
import ma.itroad.aace.eth.coref.model.entity.RegulationRef;
import ma.itroad.aace.eth.coref.service.IRegulationRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;



@Mapper(componentModel = "spring")
public abstract class RegulationRefMapper extends CommonMapper implements GenericModelMapper<RegulationRef, RegulationRefBean> {

    @Autowired
    IRegulationRefService service;

    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract RegulationRefBean entityToBean(RegulationRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}
