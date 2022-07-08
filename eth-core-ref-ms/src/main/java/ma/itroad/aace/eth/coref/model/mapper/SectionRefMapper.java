package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.service.ISectionRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
    public abstract class SectionRefMapper extends CommonMapper implements GenericModelMapper<SectionRef, SectionRefBean> {

    @Autowired
    ISectionRefService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract SectionRefBean entityToBean(SectionRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}

