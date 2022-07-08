package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.CategoryRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CategoryRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.CategoryRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.CategoryRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface ICategoryRefService extends
        IBaseService<CategoryRef, CategoryRefBean>,
        IBaseRefService,
        IRsqlService<CategoryRef, CategoryRefBean>, ImportDataService{

    Page<CategoryRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<CategoryRef> saveAll(List<CategoryRef> categoryRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long categoryRefId) ;

    ByteArrayInputStream categoryRefToExcel(List<CategoryRefEntityRefLang> categoryRefs);

    List<CategoryRefEntityRefLang> excelToCategoryRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addCategory(CategoryRefEntityRefLang categoryRefEntityRefLang);

    CategoryRefEntityRefLang findCategory(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<CategoryRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    Page<CategoryRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    CategoryRefBean findById(Long id);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);


}
