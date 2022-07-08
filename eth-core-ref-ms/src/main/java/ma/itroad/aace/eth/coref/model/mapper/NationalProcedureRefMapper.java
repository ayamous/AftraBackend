package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.NationalProcedureRefBean;
import ma.itroad.aace.eth.coref.model.entity.NationalProcedureRef;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NationalProcedureRefMapper extends GenericModelMapper<NationalProcedureRef, NationalProcedureRefBean> {
	default NationalProcedureRef map(Long id) {
		if (id == null)
			return null;
		NationalProcedureRef nationalProcedureRef = new NationalProcedureRef();
		nationalProcedureRef.setId(id);
		return nationalProcedureRef;
	}

}
