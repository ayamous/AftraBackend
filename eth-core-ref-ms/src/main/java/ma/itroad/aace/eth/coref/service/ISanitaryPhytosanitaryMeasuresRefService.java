package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.SanitaryPhytosanitaryMeasuresRefBean;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.SanitaryPhytosanitaryMeasuresRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.SanitaryPhytosanitaryMeasuresRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.MspAndBarriersFilterPayload;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubPortalMspAndBarrsFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.service.helper.CustomsOfficeRefLang;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;
import ma.itroad.aace.eth.coref.service.helper.SanitaryPhytosanitaryMeasuresRefLang;

import ma.itroad.aace.eth.coref.service.helper.detailed.SanitaryPhytosanitaryMeasuresRefLangDetailed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface ISanitaryPhytosanitaryMeasuresRefService extends

		IBaseService<SanitaryPhytosanitaryMeasuresRef, SanitaryPhytosanitaryMeasuresRefBean>,
		IBaseRefService,
		IRsqlService<SanitaryPhytosanitaryMeasuresRef, SanitaryPhytosanitaryMeasuresRefBean>, ImportDataService {

	List<SanitaryPhytosanitaryMeasuresRef> saveAll(
			List<SanitaryPhytosanitaryMeasuresRef> sanitaryPhytosanitaryMeasuresRefs);

	ErrorResponse delete(Long id);

	ErrorResponse delete(Long id, String lang);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ByteArrayInputStream load();

	SanitaryPhytosanitaryMeasuresRefLang findSanitaryPhytosanitaryMeasures(Long id, String lang);

	SanitaryPhytosanitaryMeasuresRefLangDetailed findSanitaryPhytosanitaryMeasureDetailed(SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang);

	ByteArrayInputStream load(String lang);

	Page<SanitaryPhytosanitaryMeasuresRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	Page<SanitaryPhytosanitaryMeasuresRefLang> filterForProductInformationFinder(ProductInformationFinderFilterPayload productInformationFinderFilterPayload, int page, int size, String codeLang, String orderDirection);

	Page<SanitaryPhytosanitaryMeasuresRefLang> filterForPortal(MspAndBarriersFilterPayload mspAndBarriersFilterPayload, int page, int size, String codeLang, String orderDirection);

	Page<SanitaryPhytosanitaryMeasuresRefLang> filterForSubPortal(SubPortalMspAndBarrsFilterPayload subPortalMspAndBarrsFilterPayload, int page, int size, String codeLang);

	Page<SanitaryPhytosanitaryMeasuresRefLang> subPortalProductInformationFinderfilter(SubportalProductInformationFinderFilterPayload payload, int page, int size, String codeLang, String orderDirection);

	void addSanitaryPhytosanitaryMeasuresRef(SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

	ByteArrayInputStream sanitaryPhytosanitaryMeasuresRefsToExcel(List<SanitaryPhytosanitaryMeasuresRefLang> sanitaryPhytosanitaryMeasuresRefLangs);

	List<SanitaryPhytosanitaryMeasuresRefLang> excelToSanitaryPhytosanitaryMeasuresRefs(InputStream is);

	Page<SanitaryPhytosanitaryMeasuresRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

	Page<SanitaryPhytosanitaryMeasuresRefLangProjection> filterByCodeOrLabelProjection(String value, String lang, Pageable pageable);

    SanitaryPhytosanitaryMeasuresRefBean findById(Long id);

	Set<ConstraintViolation<SanitaryPhytosanitaryMeasuresRefLang>> validateSanitaryPhytosanitaryMeasures(SanitaryPhytosanitaryMeasuresRefLang sanitaryPhytosanitaryMeasuresRefLang);

	ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);
}
