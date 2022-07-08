package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.EconomicOperatorBean;
import ma.itroad.aace.eth.coref.model.entity.EconomicOperator;
import org.mapstruct.Mapper;
 
@Mapper(componentModel = "spring")
public interface EconomicOperatorMapper extends GenericModelMapper<EconomicOperator,EconomicOperatorBean> {

}
