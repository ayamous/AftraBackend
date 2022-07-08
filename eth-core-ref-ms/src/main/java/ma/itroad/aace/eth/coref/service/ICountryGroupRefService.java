package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryGroupRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.CountryGroupRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryGroupRefsEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.CityRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.CountryGroupRefsEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ICountryGroupRefService extends
        IBaseService<CountryGroupRef, CountryGroupRefBean>,
        IBaseRefService,
        IRsqlService<CountryGroupRef, CountryGroupRefBean>, ImportDataService {

    Page<CountryGroupRefsEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<CountryGroupRef> saveAll(List<CountryGroupRef> countryGroupRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long countryGroupRefId) ;

    ByteArrayInputStream countryGroupRefsToExcel(List<CountryGroupRefsEntityRefLang> countryGroupRefs);

    List<CountryGroupRefsEntityRefLang> excelToCountryGroupRefsRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addCountryGroup(CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang);

    CountryGroupRefsEntityRefLang findCountryGroup(Long id, String lang);

    Page<CountryGroupRefsEntityRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang) ;

    CountryGroupRefBean findById(Long id);

    Page<CountryGroupRefsEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Pageable pageable, String lang);

    Set<ConstraintViolation<CountryGroupRefsEntityRefLang>> validateCountryGroupRef(CountryGroupRefsEntityRefLang countryGroupRefsEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
