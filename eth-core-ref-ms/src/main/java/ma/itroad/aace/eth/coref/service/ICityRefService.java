package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CityRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.CityRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CityRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.CityRefVM;
import ma.itroad.aace.eth.coref.service.helper.CityRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ICityRefService extends
        IBaseService<CityRef, CityRefBean>,
        IBaseRefService,
        IRsqlService<CityRef, CityRefBean>, ImportDataService {

    Page<CityRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<CityRef> saveAll(List<CityRef> cityRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long cityRefId) ;

    ByteArrayInputStream cityRefsToExcel(List<CityRefEntityRefLang> cityRefEntityRefLangs);

    List<CityRefEntityRefLang> excelToCityRefsRef(InputStream is);

    ByteArrayInputStream load(String codeLang, final int page, final int size, String orderDirection);

    void addCityRef(CityRefEntityRefLang cityRefEntityRefLang);

    CityRefEntityRefLang findcity(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang);

    Page<CityRefEntityRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang) ;

    Page<CityRefEntityRefLangProjection> filterByReferenceOrLabel(String value, String lang, int page, int size);

    CityRefBean findById(Long id);

    Set<ConstraintViolation<CityRefEntityRefLang>> validateCityRef(CityRefEntityRefLang cityRefEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
