package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.coref.model.bean.EntityRefLangBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;

public interface IEntityRefLangService extends IBaseService<EntityRefLang, EntityRefLangBean> {


    public EntityRefLangBean save(EntityRefLangBean bean);
}
