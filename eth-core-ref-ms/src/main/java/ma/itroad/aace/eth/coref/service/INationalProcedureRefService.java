package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.NationalProcedureRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.NationalProcedureRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.NationalProcedureRefLangProjection;
import ma.itroad.aace.eth.coref.model.view.NationalProcedureRefVM;
import ma.itroad.aace.eth.coref.model.view.ProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubPortalNationalProcedureAndRegulationFilterPayload;
import ma.itroad.aace.eth.coref.model.view.SubportalProductInformationFinderFilterPayload;
import ma.itroad.aace.eth.coref.service.helper.NationalProcedureRefLang;

import ma.itroad.aace.eth.coref.service.helper.PortRefLang;
import ma.itroad.aace.eth.coref.service.helper.VersionRefEntityRefLang;
import ma.itroad.aace.eth.coref.service.helper.detailed.NationalProcedureRefLangDetailed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface INationalProcedureRefService extends IBaseService<NationalProcedureRef, NationalProcedureRefBean>,
		IRsqlService<NationalProcedureRef, NationalProcedureRefBean>, ImportDataService {

	List<NationalProcedureRef> saveAll(List<NationalProcedureRef> nationalProceduresRefs);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ByteArrayInputStream nationalProcedureRefsToExcel(List<NationalProcedureRefLang> nationalProcedureRefLangs);

	List<NationalProcedureRefLang> excelToNationalProcedureRefs(InputStream is);

	public Page<NationalProcedureRefBean> getAll(int page, int size);

	//public Page<NationalProcedureRefBean> getAll(String lang, int page, int size);

	Page<NationalProcedureRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	public ByteArrayInputStream load(String codeLang);

	public NationalProcedureRefBean addInternationalisation(NationalProcedureRefLang nationalProcedureRefLang);

	NationalProcedureRefBean addNationalProcedure(NationalProcedureRefLang nationalProcedureRefLang);

	public NationalProcedureRefLang findNationalProcedure(Long id, String lang);

	public NationalProcedureRefLangDetailed findNationalProcedureDetailed(NationalProcedureRefLang nationalProcedureRefLang);

	public void addInternationalisation(EntityRefLang entityRefLang, String lang);

	Page<NationalProcedureRefLang> filterForPortal(final int page, final int size,
			ProductInformationFinderFilterPayload productInformationFinderFilterPayload, String codeLang, String orderDirection);

	Page<NationalProcedureRefLang> nationalProcedureFilter(NationalProcedureRefVM nationalProcedureRefVM, int page,
			int size, String codeLang, String orderDirection);

	Page<NationalProcedureRefLang> procedureSubportalProductInformationFinderFilter(
			SubportalProductInformationFinderFilterPayload subportalProductInformationFinderFilterPayload, int page,
			int size, String codeLang, String orderDirection);

	Page<NationalProcedureRefLang> filterForSubPortal(
			SubPortalNationalProcedureAndRegulationFilterPayload subPortalNationalProcedureAndRegulationFilterPayload,
			int page, int size, String codeLang);

	Page<NationalProcedureRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

	Page<NationalProcedureRefLangProjection> filterByCodeOrLabelProjection(String value, String lang, int page, int size);

    NationalProcedureRefBean findById(Long id);

	//Set<ConstraintViolation<NationalProcedureRefLang>> validateNationalProcedureRefLang(NationalProcedureRefLang nationalProcedureRefLang);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);

	Set<ConstraintViolation<NationalProcedureRefLang>> validateNationalProcedure(NationalProcedureRefLang nationalProcedureRefLang);


}
