package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.coref.model.bean.ESafeTagsBean;
import ma.itroad.aace.eth.coref.model.entity.ESafeTags;
 
public interface IESafeTagsService extends
        IBaseService<ESafeTags,ESafeTagsBean>,
        IRsqlService<ESafeTags,ESafeTagsBean> {
}