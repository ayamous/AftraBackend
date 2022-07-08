package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.ESafeTagsBean;
import ma.itroad.aace.eth.coref.model.entity.ESafeTags;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface ESafeTagsMapper extends GenericModelMapper<ESafeTags,ESafeTagsBean> {

}
