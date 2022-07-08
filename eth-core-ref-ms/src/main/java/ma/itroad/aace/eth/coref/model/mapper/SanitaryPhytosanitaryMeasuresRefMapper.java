package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.SanitaryPhytosanitaryMeasuresRefBean;
import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.service.ISanitaryPhytosanitaryMeasuresRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SanitaryPhytosanitaryMeasuresRefMapper extends CommonMapper implements GenericModelMapper<SanitaryPhytosanitaryMeasuresRef, SanitaryPhytosanitaryMeasuresRefBean> {

    @Autowired
    ISanitaryPhytosanitaryMeasuresRefService service;

    /*
     default SanitaryPhytosanitaryMeasuresRef map(Long id) {
        if (id == null)    return null;
        SanitaryPhytosanitaryMeasuresRef sanitaryPhytosanitaryMeasuresRef = new SanitaryPhytosanitaryMeasuresRef();
        sanitaryPhytosanitaryMeasuresRef.setId(id);
        return sanitaryPhytosanitaryMeasuresRef;
    }
     */
    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract SanitaryPhytosanitaryMeasuresRefBean entityToBean(SanitaryPhytosanitaryMeasuresRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}