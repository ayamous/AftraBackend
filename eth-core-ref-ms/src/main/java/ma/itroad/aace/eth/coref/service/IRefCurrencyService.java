package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefCurrencyBean;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.RefCurrency;
import ma.itroad.aace.eth.coref.model.entity.SectionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.RefCurrencyRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.RefCurrencyRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.UnitRefEntityRefLang;
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

public interface IRefCurrencyService extends
        IBaseService<RefCurrency, RefCurrencyBean>,
        IBaseRefService,
        IRsqlService<RefCurrency, RefCurrencyBean>, ImportDataService {

    Page<RefCurrencyRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<RefCurrency> saveAll(List<RefCurrency> refCurrencies);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long refCurrencyId) ;

    ByteArrayInputStream currenciesToExcel(List<RefCurrencyRefEntityRefLang> refCurrencies);

    List<RefCurrencyRefEntityRefLang> excelToCurrencies(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addRefCurrencyRef(RefCurrencyRefEntityRefLang refCurrencyRefEntityRefLang);

    RefCurrencyRefEntityRefLang findCurrency(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<RefCurrencyRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    RefCurrencyBean findById(Long id);

    Page<RefCurrencyRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    Set<ConstraintViolation<RefCurrencyRefEntityRefLang>> validateCurrency(RefCurrencyRefEntityRefLang refCurrencyRefEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
