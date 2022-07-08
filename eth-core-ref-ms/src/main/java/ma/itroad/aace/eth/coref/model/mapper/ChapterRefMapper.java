package ma.itroad.aace.eth.coref.model.mapper;
 
import ma.itroad.aace.eth.core.model.bean.InternationalizationVM;
import ma.itroad.aace.eth.core.model.mapper.GenericModelMapper;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.service.IChapterRefService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ChapterRefMapper extends CommonMapper implements GenericModelMapper<ChapterRef, ChapterRefBean> {

    @Autowired
    IChapterRefService service;

    /*
     default ChapterRef map(Long id) {
     if (id == null)    return null;
     ChapterRef chapterRef = new ChapterRef();
     chapterRef.setId(id);
     return chapterRef;
     }
     */
    @Override
    @Mapping(source = "id", target = "internationalizationVMList", qualifiedByName = "internationalizationRefList")
    public abstract ChapterRefBean entityToBean(ChapterRef entity);


    @Override
    @Named("internationalizationRefList")
    public List<InternationalizationVM> internationalizationCountryRefList(Long id) throws InstantiationException, IllegalAccessException {
        internationalizationService.setService(service);
        return internationalizationService.internationalizationRefList(id);
    }

}

