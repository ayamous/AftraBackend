package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.PersonalContactBean;
import ma.itroad.aace.eth.coref.model.entity.PersonalContact;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface PersonalContactMapper extends GenericModelMapper<PersonalContact,PersonalContactBean> {

}
