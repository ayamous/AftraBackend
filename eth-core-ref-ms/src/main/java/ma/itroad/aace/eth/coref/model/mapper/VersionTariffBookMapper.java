package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.VersionTariffBookBean;
import ma.itroad.aace.eth.coref.model.entity.VersionTariffBookRef;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VersionTariffBookMapper extends GenericModelMapper<VersionTariffBookRef, VersionTariffBookBean> {

}
