package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.CustomsOfficeRefBean;
import ma.itroad.aace.eth.coref.model.entity.CustomsOfficeRef;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface CustomsOfficeRefMapper extends GenericModelMapper<CustomsOfficeRef,CustomsOfficeRefBean> {

}
