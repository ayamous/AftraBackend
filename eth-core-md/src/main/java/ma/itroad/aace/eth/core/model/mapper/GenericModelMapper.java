package ma.itroad.aace.eth.core.model.mapper;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.entity.AuditEntity;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface GenericModelMapper<Entity extends AuditEntity, Bean extends Serializable> {


    //@Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    Bean entityToBean(Entity entity);

    Entity beanToEntity(Bean bean);

    List<Bean> toBeanList(List<Entity> entities);

    List<Entity> toEntityList(List<Bean> beans);

}
