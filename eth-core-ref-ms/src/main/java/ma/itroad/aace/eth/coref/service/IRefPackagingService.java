package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RefPackagingBean;
import ma.itroad.aace.eth.coref.model.bean.SectionRefBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.projections.RefPackagingEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.RefPackagingEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.SectionRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.TransportationTypeEntityRefLang;
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

public interface IRefPackagingService extends
        IBaseService<RefPackaging, RefPackagingBean>,
        IBaseRefService,
        IRsqlService<RefPackaging, RefPackagingBean>, ImportDataService{


    Page<RefPackagingEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<RefPackaging> saveAll(List<RefPackaging> refPackagings);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long refPackagingId) ;

    ByteArrayInputStream refPackagingsToExcel(List<RefPackagingEntityRefLang> refPackagings);

    List<RefPackagingEntityRefLang> excelToRefPackaging(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addRefPackaging(RefPackagingEntityRefLang refPackagingEntityRefLang);

    RefPackagingEntityRefLang findRefPackaging(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<RefPackagingEntityRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    ErrorResponse deleteList(ListOfObject listOfObject);

    RefPackagingBean findById(Long id);

    Page<RefPackagingEntityRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    Set<ConstraintViolation<RefPackagingEntityRefLang>> validatePackaging(RefPackagingEntityRefLang refPackagingEntityRefLang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
