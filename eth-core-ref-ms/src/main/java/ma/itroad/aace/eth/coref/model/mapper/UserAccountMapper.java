package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.UserAccountBean;
import ma.itroad.aace.eth.coref.model.entity.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAccountMapper extends GenericModelMapper<UserAccount, UserAccountBean> {
}
