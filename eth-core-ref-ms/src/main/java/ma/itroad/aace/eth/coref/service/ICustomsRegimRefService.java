package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CustomsRegimRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CustomsRegimRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.CustomsRegimRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.CustomsRegimRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.DeclarationTypeRefEntityRefLang;
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

public interface ICustomsRegimRefService extends
        IBaseService<CustomsRegimRef, CustomsRegimRefBean>,
        IBaseRefService,
        IRsqlService<CustomsRegimRef, CustomsRegimRefBean>, ImportDataService{

    Page<CustomsRegimRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<CustomsRegimRef> saveAll(List<CustomsRegimRef> customsRegimRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long customsRegimRefId) ;

    ByteArrayInputStream customsRegimRefToExcel(List<CustomsRegimRefEntityRefLang> customsRegimRefs);

    List<CustomsRegimRefEntityRefLang> excelToCustomsRegimRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addCustomsRegimRef(CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang);

    CustomsRegimRefEntityRefLang findCustomsRegimRef(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<CustomsRegimRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    CustomsRegimRefBean findById(Long id);

    Page<CustomsRegimRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    CustomsRegimRefBean updateItem(Long id,CustomsRegimRefBean bean);

    Set<ConstraintViolation<CustomsRegimRefEntityRefLang>> validateCustomsRegim(CustomsRegimRefEntityRefLang customsRegimRefEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
