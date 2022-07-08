package ma.itroad.aace.eth.coref.model.mapper;

import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.impl.InternationalizationService;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonMapper {

    @Autowired
    protected InternationalizationService internationalizationService;

    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        return new ArrayList<InternationalizationVM>();
    }

}
