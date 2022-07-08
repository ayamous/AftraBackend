package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.model.enums.TableRef;
import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.NationalProcedureRefBean;
import ma.itroad.aace.eth.coref.model.bean.TechBarrierRefBean;
import ma.itroad.aace.eth.coref.model.bean.VersionBean;
import ma.itroad.aace.eth.coref.model.entity.*;
import ma.itroad.aace.eth.coref.model.mapper.projections.TechnicalBarrierRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.MspAndBarriersFilterPayload;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;

import ma.itroad.aace.eth.coref.model.view.SubPortalMspAndBarrsFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.service.helper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import ma.itroad.aace.eth.coref.service.helper.detailed.RegulationRefLangDetailed;
import ma.itroad.aace.eth.coref.service.helper.detailed.TechnicalBarrierRefLangDetailed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface ITechBarrierRefService extends
		IBaseService<TechBarrierRef, TechBarrierRefBean>,
		IBaseRefService,
		IRsqlService<TechBarrierRef, TechBarrierRefBean>, ImportDataService{

	Page<TechnicalBarrierRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	List<TechBarrierRef> saveAll(List<TechBarrierRef> techBarrierRefs);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ErrorResponse deleteInternationalisation(String codeLang, Long techBarrierRefId) ;

	ByteArrayInputStream techBarrierRefsToExcel(List<TechnicalBarrierRefLang> technicalBarrierRefLangs);

	List<TechnicalBarrierRefLang> excelToTechBarrierRefs(InputStream is);

	ByteArrayInputStream load(String codeLang);

	void addTechnicalBarrier(TechnicalBarrierRefLang technicalBarrierRefLang);

	TechnicalBarrierRefLang findTechnicalBarrier(Long id, String lang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

	Page<TechnicalBarrierRefLang> techBarFilterByTarifBook(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection);

	Page<TechnicalBarrierRefLang> techBarFilter(MspAndBarriersFilterPayload mspAndBarriersFilterPayload, int page, int size, String codeLang, String orderDirection);

	Page<TechnicalBarrierRefLang> filterForSubPortal(SubPortalMspAndBarrsFilterPayload subPortalMspAndBarrsFilterPayload,int page, int size, String codeLang);

	Page<TechnicalBarrierRefLang> subPortalProductInformationFinderfilter(SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang, String orderDirection);

	Page<TechnicalBarrierRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

	TechnicalBarrierRefLangDetailed findTechnicalBarrierDetailedDetailed(TechnicalBarrierRefLang technicalBarrierRefLang);

    Page<TechnicalBarrierRefLangProjection> filterByCodeOrLabelProjection(String value, String lang, Pageable pageable);

    TechBarrierRefBean findById(Long id);
}
