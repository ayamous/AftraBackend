package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.AuthorizationSetupBean;
import ma.itroad.aace.eth.coref.model.entity.AuthorizationSetup;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface AuthorizationSetupMapper extends GenericModelMapper<AuthorizationSetup,AuthorizationSetupBean> {

}
