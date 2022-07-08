package ma.itroad.aace.eth.coref.service.helper;

import lombok.Getter;
import lombok.Setter;
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.repository.EntityRefLangRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Getter
@Setter
public class InternationalizationHelper {


    private final EntityRefLangRepository entityRefLangRepository;

    public InternationalizationHelper(EntityRefLangRepository entityRefLangRepository) {
        this.entityRefLangRepository = entityRefLangRepository;

    }

    public static InternationalizationVM toInternationalizationVM(EntityRefLang entityRefLang) {

        InternationalizationVM vm = new InternationalizationVM();
        vm.setDescription(entityRefLang.getDescription());
        vm.setLabel(entityRefLang.getLabel());
        vm.setLanguage(entityRefLang.getLang().getName());
        return vm;
    }

    public List<InternationalizationVM> getInternationalizationRefList(Long id, TableRef tableRef) {

        List<EntityRefLang> entityRefLangs = entityRefLangRepository.findByTableRefAndRefId(tableRef, id);
        List<InternationalizationVM> internationalizationVMList = entityRefLangs.stream().map(InternationalizationHelper::toInternationalizationVM).collect(Collectors.toList());
        return internationalizationVMList;
    }


}
