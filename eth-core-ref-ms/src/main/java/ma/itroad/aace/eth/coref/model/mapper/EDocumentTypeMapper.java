package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.EDocumentTypeBean;
import ma.itroad.aace.eth.coref.model.entity.EDocumentType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EDocumentTypeMapper extends GenericModelMapper<EDocumentType, EDocumentTypeBean> {
}
