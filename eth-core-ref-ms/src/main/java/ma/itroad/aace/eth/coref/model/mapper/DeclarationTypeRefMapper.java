package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.DeclarationTypeRefBean;
import ma.itroad.aace.eth.coref.model.entity.DeclarationTypeRef;
import ma.itroad.aace.eth.coref.service.IDeclarationTypeRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class DeclarationTypeRefMapper extends CommonMapper implements GenericModelMapper<DeclarationTypeRef, DeclarationTypeRefBean> {

    @Autowired
    IDeclarationTypeRefService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract DeclarationTypeRefBean entityToBean(DeclarationTypeRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}