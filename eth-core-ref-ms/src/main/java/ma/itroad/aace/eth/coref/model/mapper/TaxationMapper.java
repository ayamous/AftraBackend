package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.TaxationBean;
import ma.itroad.aace.eth.coref.model.entity.Taxation;
import ma.itroad.aace.eth.coref.service.ITaxationService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TaxationMapper extends CommonMapper implements GenericModelMapper<Taxation, TaxationBean> {

	@Autowired
	ITaxationService service;

	/*
    default Taxation map(Long id) {
		if (id == null)
			return null;
		Taxation taxation = new Taxation();
		taxation.setId(id);
		return taxation;
	}
     */
	@Override
	@Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
	public abstract TaxationBean entityToBean(Taxation entity);


	@Override
	@Named("internationalizationRefList")
	public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
		internationalizationService.setService(service);
		return internationalizationService.internationalizationRefList(id);
	}

}