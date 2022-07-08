package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.UnitRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.UnitRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.UnitRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.LangEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.UnitRefEntityRefLang;
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

public interface IUnitRefService extends
        IBaseService<UnitRef, UnitRefBean>,
        IBaseRefService,
        IRsqlService<UnitRef, UnitRefBean>, ImportDataService{

    Page<UnitRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<UnitRef> saveAll(List<UnitRef> unitRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long unitRefId) ;

    ByteArrayInputStream unitrefsToExcel(List<UnitRefEntityRefLang> unitRefs);

    List<UnitRefEntityRefLang> excelToUnitsRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addUnitRef(UnitRefEntityRefLang unitRefEntityRefLang);

    UnitRefEntityRefLang findUnitRef(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<UnitRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    Page<UnitRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    UnitRefBean findById(Long id);

	Set<ConstraintViolation<UnitRefEntityRefLang>> validateUnits(UnitRefEntityRefLang unitRefEntityRefLang);

	ResponseEntity<?> saveFromAfterValidation(MultipartFile file);
}
