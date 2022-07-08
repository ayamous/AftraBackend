package ma.itroad.aace.eth.coref.model.mapper;

        import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
        import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
        import ma.itroad.aace.eth.coref.model.bean.AgreementTypeBean;
        import ma.itroad.aace.eth.coref.model.entity.AgreementType;
        import ma.itroad.aace.eth.coref.service.IAgreementTypeService;
        import org.mapstruct.Mapper;
        import org.mapstruct.Mapping;
        import org.mapstruct.Named;
        import org.springframework.beans.factory.annotation.Autowired;

        import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AgreementTypeMapper extends CommonMapper implements GenericModelMapper<AgreementType, AgreementTypeBean> {

    @Autowired
    IAgreementTypeService service;

    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract AgreementTypeBean entityToBean(AgreementType entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}


