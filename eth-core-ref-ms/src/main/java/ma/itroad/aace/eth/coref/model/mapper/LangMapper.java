package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.LangBean;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.service.ILangService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LangMapper extends CommonMapper implements GenericModelMapper<Lang, LangBean> {

    @Autowired
    ILangService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract LangBean entityToBean(Lang entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}