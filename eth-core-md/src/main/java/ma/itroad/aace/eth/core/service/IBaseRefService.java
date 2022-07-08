package ma.itroad.aace.eth.core.service;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;

import java.util.List;

public interface IBaseRefService {

    List<InternationalizationVM> getInternationalizationRefList(Long id);

}
