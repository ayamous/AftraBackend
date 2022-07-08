package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.SectionRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface ISectionRefService extends
        IBaseService<SectionRef, SectionRefBean>,
        IBaseRefService,
        IRsqlService<SectionRef, SectionRefBean>, ImportDataService {

    Page<SectionRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<SectionRef> saveAll(List<SectionRef> sectionRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long sectionRefId) ;

    ByteArrayInputStream sectionsToExcel(List<SectionRefEntityRefLang> sectionRefs);

    List<SectionRefEntityRefLang> excelToSectionsRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addSectionRef(SectionRefEntityRefLang sectionRefEntityRefLang);

    SectionRefEntityRefLang findSection(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<SectionRefEntityRefLang> filterByCodeOrLabel(String value, int page,int size, String codeLang);

    Page<SectionRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    SectionRefBean findById(Long id);

    ResponseEntity saveFromExcelWithReturn(MultipartFile file);

}
