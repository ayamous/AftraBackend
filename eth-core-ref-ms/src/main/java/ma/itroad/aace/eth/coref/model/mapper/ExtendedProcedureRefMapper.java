package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.ExtendedProcedureRefBean;
import ma.itroad.aace.eth.coref.model.entity.ExtendedProcedureRef;
import ma.itroad.aace.eth.coref.service.IExtendedProcedureRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ExtendedProcedureRefMapper extends CommonMapper implements GenericModelMapper<ExtendedProcedureRef, ExtendedProcedureRefBean> {

	@Autowired
	IExtendedProcedureRefService service;

	/*
     default ExtendedProcedureRef map(Long id) {
		if (id == null)
			return null;
		ExtendedProcedureRef extendedProcedureRef = new ExtendedProcedureRef();
		extendedProcedureRef.setId(id);
		return extendedProcedureRef;
	}
     */
	@Override
	@Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
	public abstract ExtendedProcedureRefBean entityToBean(ExtendedProcedureRef entity);


	@Override
	@Named("internationalizationRefList")
	public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
		internationalizationService.setService(service);
		return internationalizationService.internationalizationRefList(id);
	}

}


