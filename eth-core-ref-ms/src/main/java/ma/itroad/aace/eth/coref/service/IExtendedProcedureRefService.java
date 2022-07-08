package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ChapterRefBean;
import ma.itroad.aace.eth.coref.model.bean.ExtendedProcedureRefBean;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.entity.ChapterRef;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.ExtendedProcedureRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.ExtendedProcedureRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.ExtendedProcedureRefLang;

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

public interface IExtendedProcedureRefService extends
		IBaseService<ExtendedProcedureRef, ExtendedProcedureRefBean>,
		IBaseRefService,
		IRsqlService<ExtendedProcedureRef, ExtendedProcedureRefBean>, ImportDataService{


	Page<ExtendedProcedureRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	List<ExtendedProcedureRef> saveAll(List<ExtendedProcedureRef> extendedProcedureRefs);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ErrorResponse deleteInternationalisation(String codeLang, Long extendedProcedureId) ;

	ByteArrayInputStream extendedProcedureRefsToExcel(List<ExtendedProcedureRefLang> extendedProcedureRefLangs);

	List<ExtendedProcedureRefLang> excelToExtendedProcedureRefs(InputStream is);

	ByteArrayInputStream load(String codeLang);

	void addExtendedProcedure(ExtendedProcedureRefLang extendedProcedureRefLang);

	ExtendedProcedureRefLang findExtendedProcedure(Long id, String lang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

	Page<ExtendedProcedureRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

	Set<ConstraintViolation<ExtendedProcedureRefLang>> validateExtendedProcedure(ExtendedProcedureRefLang extendedProcedureRefLang);

	ResponseEntity<?> saveFromExcelWithReturn(MultipartFile file);

}
