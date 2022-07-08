package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.TaxationBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.Taxation;
import ma.itroad.aace.eth.coref.model.mapper.projections.TaxationRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.service.helper.TaxationRefLang;

import ma.itroad.aace.eth.coref.service.helper.TechnicalBarrierRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.TaxationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.TechnicalBarrierRefLangDetailed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface ITaxationService extends
		IBaseService<Taxation, TaxationBean>,
		IBaseRefService,
		IRsqlService<Taxation, TaxationBean>, ImportDataService{


	Page<TaxationRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	List<Taxation> saveAll(List<Taxation> taxations);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ErrorResponse deleteInternationalisation(String codeLang, Long taxationId) ;

	ByteArrayInputStream taxationsToExcel(List<TaxationRefLang> taxationRefLangs);

	List<TaxationRefLang> excelToTaxations(InputStream is);

	ByteArrayInputStream load(String codeLang);

	void addTaxation(TaxationRefLang taxationRefLang);

	TaxationRefLang findTaxation(Long id, String lang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

	//Page<TaxationBean> getAll(final int page, final int size);

	Page<TaxationRefLang> filterForPortal(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection);

	Page<TaxationRefLang> subPortalProductInformationFinderfilter(SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang, String orderDirection);

	Page<TaxationRefLang> filterByReferenceOrLabel(String value, Pageable pageable, String codeLang);

	TaxationRefLangDetailed findTaxationDetailedDetailed(TaxationRefLang taxationRefLang);

	Page<TaxationRefLangProjection> filterByReferenceOrLabelProjection(String value, String lang, Pageable pageable);

    TaxationBean findById(Long id);

	ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
