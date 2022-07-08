package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CountryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.CountryRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ICountryRefService extends
        IBaseService<CountryRef, CountryRefBean>,
        IBaseRefService,
        IRsqlService<CountryRef, CountryRefBean>,
        ImportDataService {

    Page<CountryRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<CountryRef> saveAll(List<CountryRef> countryRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long countryRefId) ;

    ByteArrayInputStream countryRefsToExcel(List<CountryRefEntityRefLang> countryRefs);

    List<CountryRefEntityRefLang> excelToCountryRefsRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addCountryRef(CountryRefEntityRefLang countryRefEntityRefLang);

    CountryRefEntityRefLang findCountry(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<CountryRefEntityRefLang> filterByReferenceOrLabel(String value,  int page,int size, String codeLang) ;

    Page<CountryRefEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Pageable pageable, String lang);

    CountryRefBean findById(Long id);

    Set<ConstraintViolation<CountryRefEntityRefLang>> validateCountryRef(CountryRefEntityRefLang countryRefEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}