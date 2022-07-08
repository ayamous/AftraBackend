package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.OrganizationBean;
import ma.itroad.aace.eth.coref.model.entity.Organization;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface OrganizationMapper extends GenericModelMapper<Organization,OrganizationBean> {

}
