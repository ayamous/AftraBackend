package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.VersionBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.VersionRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.VersionRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import ma.itroad.aace.eth.coref.service.helper.VersionRefEntityRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IVersionService extends
        IBaseService<VersionRef, VersionBean>,
        IBaseRefService,
        IRsqlService<VersionRef, VersionBean>, ImportDataService {

    Page<VersionRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<VersionRef> saveAll(List<VersionRef> versionRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteInternationalisation(String codeLang, Long versionRefId) ;

    ByteArrayInputStream versionsToExcel(List<VersionRefEntityRefLang> versionRefs);

    List<VersionRefEntityRefLang> excelToVersionRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addVersionRef(VersionRefEntityRefLang versionRefEntityRefLang);

    VersionRefEntityRefLang findVersionRef(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<VersionRefEntityRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang);

    VersionBean findById(Long id);

    Page<VersionRefEntityRefLangProjection> filterByReferenceOrLabelProjection(String value, Pageable pageable, String lang);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
