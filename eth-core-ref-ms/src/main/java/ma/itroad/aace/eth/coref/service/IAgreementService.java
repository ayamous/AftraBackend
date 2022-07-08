package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.AgreementBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.Agreement;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.mapper.projections.AgreementLangProjection;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.TradeAgreementFilterPayload;
import ma.itroad.aace.eth.coref.service.helper.AgreementLang;
import ma.itroad.aace.eth.coref.service.helper.SanitaryPhytosanitaryMeasuresRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.AgreementLangDetailed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface IAgreementService extends
        IBaseService<Agreement, AgreementBean>,
        IBaseRefService,
        IRsqlService<Agreement, AgreementBean>, ImportDataService{

    List<Agreement> saveAll(List<Agreement> agreements);

    ErrorResponse delete(Long id);

    ErrorResponse deleteList(ListOfObject listOfObject);

    ByteArrayInputStream agreementsToExcel(List<AgreementLang> agreementLangs);

    List<AgreementLang> excelToAgreements(InputStream is);

    ByteArrayInputStream load(String codeLang);

    Page<AgreementLang> getAll(final int page, final int size, String codeLang, String orderDirection);

    Page<AgreementLang> filterForPortal(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection);

    Page<AgreementLang> subPortalProductInformationFinderfilter(final int page, final int size, SubportalProductInformationFinderFilterPayload payload, String lang, String orderDirection);

    Page<AgreementLang> tradeAgreementFilter(TradeAgreementFilterPayload tradeAgreementFilterPayload, int page, int size, String lang);

    Page<AgreementLangProjection> tradeAgreementFilterProjection(TradeAgreementFilterPayload tradeAgreementFilterPayload, int page, int size, String lang);


    void addAgreement(AgreementLang agreementLang);

    void addInternationalisation(EntityRefLang entityRefLang, String agreementLang);

    ErrorResponse deleteInternationalisation(String codeLang, Long agreementId) ;

    AgreementLang findAgreement(Long id, String lang);

    AgreementLangDetailed findAgreementDetailed(AgreementLang agreementLang ) ;

    Page<AgreementLangProjection>  filterByCodeOrLabel(String value, Pageable pageable, String codeLang) ;

    AgreementBean findById(Long id);

    ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
