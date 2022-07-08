package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.EntityRefLangBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { LangMapper.class })
public interface EntityRefLangMapper extends GenericModelMapper<EntityRefLang, EntityRefLangBean> {


    @Override
    @Mapping(source = "lang.id", target = "langId")
    EntityRefLangBean entityToBean(EntityRefLang entityRefLang);

    @Override
    @Mapping(source = "langId", target = "lang.id")
    EntityRefLang beanToEntity(EntityRefLangBean bean);
}
