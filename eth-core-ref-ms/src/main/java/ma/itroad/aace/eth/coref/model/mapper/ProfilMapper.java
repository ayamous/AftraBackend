package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.ProfilBean;
import ma.itroad.aace.eth.coref.model.entity.Profil;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfilMapper extends GenericModelMapper<Profil, ProfilBean> {
}
