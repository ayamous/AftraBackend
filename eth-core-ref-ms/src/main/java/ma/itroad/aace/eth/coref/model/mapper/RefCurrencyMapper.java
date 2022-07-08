package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.RefCurrencyBean;
import ma.itroad.aace.eth.coref.model.entity.RefCurrency;
import ma.itroad.aace.eth.coref.service.IRefCurrencyService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class RefCurrencyMapper extends CommonMapper implements GenericModelMapper<RefCurrency, RefCurrencyBean> {

    @Autowired
    IRefCurrencyService service;


    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract RefCurrencyBean entityToBean(RefCurrency entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}

