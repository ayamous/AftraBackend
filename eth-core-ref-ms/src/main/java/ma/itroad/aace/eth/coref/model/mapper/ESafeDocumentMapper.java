package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.ESafeDocumentBean;
import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.view.ESafeDocumentVM;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface ESafeDocumentMapper extends GenericModelMapper<ESafeDocument, ESafeDocumentBean> {

}
