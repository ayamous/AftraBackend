package ma.itroad.aace.eth.coref.service;
 
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.RegulationRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.RegulationRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.RegulationRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.*;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.RegulationRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.RegulationRefLangDetailed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

public interface IRegulationRefService extends
        IBaseService<RegulationRef, RegulationRefBean>,
        IBaseRefService,
        IRsqlService<RegulationRef, RegulationRefBean>, ImportDataService{

    Page<RegulationRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    List<RegulationRef> saveAll(List<RegulationRef> regulationRefs);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ErrorResponse deleteInternationalisation(String codeLang, Long regulationRefId) ;

    ByteArrayInputStream regulationRefToExcel(List<RegulationRefLang> regulationRefLangs);

    List<RegulationRefLang> excelToRegulationRef(InputStream is);

    ByteArrayInputStream load(String codeLang);

    void addRegulationRef(RegulationRefLang regulationRefLang);

    RegulationRefLang findRegulationRef(Long id, String lang);

    void addInternationalisation(EntityRefLang entityRefLang, String lang);

    Page<RegulationRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

    Page<RegulationRefLang> filterForPortal(MspAndBarriersFilterPayload mspAndBarriersFilterPayload, int page, int size, String codeLang, String orderDirection);

    Page<RegulationRefLang> filterForProductInformationFinder(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection);

    Page<RegulationRefLang> filterForSubPortal(SubPortalMspAndBarrsFilterPayload subPortalMspAndBarrsFilterPayload, int page, int size, String codeLang);

    Page<RegulationRefLang> subPortalProductInformationFinderfilter(SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang, String orderDirection);

    RegulationRefLangDetailed findRegulationDetailed(RegulationRefLang regulationRefLang );

    RegulationRefBean findById(Long id);

    Page<RegulationRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

	Set<ConstraintViolation<RegulationRefLang>> validateRegulation(RegulationRefLang regulationRefLang);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}