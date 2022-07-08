package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.EDocumentTypeBean;
import ma.itroad.aace.eth.coref.model.bean.EntityRefLangBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.CountryRef;
import ma.itroad.aace.eth.coref.model.entity.EDocumentType;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.EDocTypeRefEntityRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.CountryRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.EDocTypeRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IEDocumentTypeService extends
        IBaseService<EDocumentType, EDocumentTypeBean>,
        ImportDataService {


    List<EDocumentType> saveAll(List<EDocumentType> eDocumentTypes);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long eDocumentTypeId) ;

    ByteArrayInputStream eDocumentTypeRefsToExcel(List<EDocTypeRefEntityRefLang> eDocTypeRefEntityRefLangs);

    List<EDocTypeRefEntityRefLang> excelToDocTypeRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addEDocType(EDocTypeRefEntityRefLang eDocTypeRefEntityRefLang);

    EDocTypeRefEntityRefLang findEDocumentTypeRefLang(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang );

    Page<EDocTypeRefEntityRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    EDocTypeRefEntityRefLang findeDocumentType(Long id, String lang);

    Page<EDocTypeRefEntityRefLangProjection> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang) ;


}
