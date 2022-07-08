package ma.itroad.aace.eth.core.service.impl;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class InternationalizationService<S extends IBaseRefService> {

    private S service;

    public InternationalizationService() {
    }


    public List<InternationalizationVM> internationalizationRefList(Long id) {

        return service.getInternationalizationRefList(id);
    }
}
