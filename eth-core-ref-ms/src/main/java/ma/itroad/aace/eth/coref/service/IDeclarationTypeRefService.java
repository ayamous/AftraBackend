package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.DeclarationTypeRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.DeclarationTypeRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.DeclarationTypeRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.DeclarationTypeRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.RefPackagingEntityRefLang;
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

public interface IDeclarationTypeRefService extends
        IBaseService<DeclarationTypeRef, DeclarationTypeRefBean>,
        IBaseRefService,
        IRsqlService<DeclarationTypeRef, DeclarationTypeRefBean>, ImportDataService{

    Page<DeclarationTypeRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<DeclarationTypeRef> saveAll(List<DeclarationTypeRef> declarationTypeRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long declarationTypeRefId) ;

    ByteArrayInputStream declarationTypeRefToExcel(List<DeclarationTypeRefEntityRefLang> declarationTypeRefs);

    List<DeclarationTypeRefEntityRefLang> excelToDeclarationTypeRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addDeclarationType(DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang);

    DeclarationTypeRefEntityRefLang findDeclarationtype(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<DeclarationTypeRefEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    DeclarationTypeRefBean findById(Long id);

    Page<DeclarationTypeRefEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    Set<ConstraintViolation<DeclarationTypeRefEntityRefLang>> validateDeclarationType(DeclarationTypeRefEntityRefLang declarationTypeRefEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
