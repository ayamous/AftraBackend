package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.LangBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Lang;
import ma.itroad.aace.eth.coref.model.mapper.projections.LangEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.LangEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface ILangService extends
        IBaseService<Lang, LangBean>,
        IBaseRefService,
        IRsqlService<Lang, LangBean>, ImportDataService{

    Page<LangEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<Lang> saveAll(List<Lang> langs);

    List<EntityRefLang> findByTableRefAndRefId(TableRef tableRef, Long id);

      ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long langsId) ;

   ByteArrayInputStream langsToExcel(List<LangEntityRefLang> langs);

    List<LangEntityRefLang> excelToLangsRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addLang(LangEntityRefLang sectionRefEntityRefLang);

    LangEntityRefLang findLang(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<LangEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang) ;

    ErrorResponse deleteList(ListOfObject listOfObject);

    LangBean findById(Long id);

    Page<LangEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

	Set<ConstraintViolation<LangEntityRefLang>> validateLangEntityRefLang(LangEntityRefLang langEntityRefLang);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
