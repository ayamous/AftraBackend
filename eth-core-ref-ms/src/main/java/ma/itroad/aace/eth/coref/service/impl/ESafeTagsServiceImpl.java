package ma.itroad.aace.eth.coref.service.impl;

import ma.itroad.aace.eth.coref.model.bean.ESafeTagsBean;
import ma.itroad.aace.eth.coref.model.entity.ESafeTags;
import ma.itroad.aace.eth.coref.service.IESafeTagsService;
import ma.itroad.aace.eth.core.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ESafeTagsServiceImpl extends BaseServiceImpl<ESafeTags,ESafeTagsBean> implements IESafeTagsService {
}