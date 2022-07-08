package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.RefTransportationTypeBean;
import ma.itroad.aace.eth.coref.model.entity.RefTransportationType;
import ma.itroad.aace.eth.coref.service.IRefTransportationTypeService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class RefTransportationTypeMapper extends CommonMapper implements GenericModelMapper<RefTransportationType, RefTransportationTypeBean> {

    @Autowired
    IRefTransportationTypeService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract RefTransportationTypeBean entityToBean(RefTransportationType entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}

