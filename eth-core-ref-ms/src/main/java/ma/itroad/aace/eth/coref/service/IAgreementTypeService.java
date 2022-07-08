package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementTypeBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.AgreementType;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementTypeLangProjection;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.AgreementTypeLang;
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

public interface IAgreementTypeService extends
        IBaseService<AgreementType, AgreementTypeBean>,
        IBaseRefService,
        IRsqlService<AgreementType, AgreementTypeBean>, ImportDataService{


    List<AgreementType> saveAll(List<AgreementType> agreementTypes);

    Page<AgreementTypeLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long agreementTypeId) ;

    ByteArrayInputStream agreementTypesToExcel(List<AgreementTypeLang> agreementTypeLangs);

    List<AgreementTypeLang> excelToAgreementTypes(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addAgreementType(AgreementTypeLang agreementTypeLang);

    void addInternationalisation(EntityRefLang entityRefLang, String agreementLang);

    AgreementTypeLang findAgreementType(Long id, String lang);

    Page<AgreementTypeLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    Page<AgreementTypeLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

    AgreementTypeBean findById(Long id);

	Set<ConstraintViolation<AgreementTypeLang>> validateAgreementType(AgreementTypeLang agreementTypeLang);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);

//    List<AgreementType> saveAll(List<AgreementType> agreements);
//    void delete(Long id);
//    ByteArrayInputStream agreementsToExcel(List<AgreementType> agreements);
//    List<AgreementType> excelToagreements(InputStream is);
//    ByteArrayInputStream load();
//    Page<AgreementTypeBean> getAll(final int page, final int size);
//    Page<AgreementTypeBean> filterForPortal(final int page, final int size, ProductInformationFinderFilterPayload productInformationFinderFilterPayload);
}