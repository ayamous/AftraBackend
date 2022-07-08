package ma.itroad.aace.eth.coref.service;

import ma.itroad.aace.eth.core.rsql.service.IRsqlService;
import ma.itroad.aace.eth.core.service.IBaseRefService;
import ma.itroad.aace.eth.core.service.IBaseService;
import ma.itroad.aace.eth.core.service.ImportDataService;
import ma.itroad.aace.eth.coref.exception.ErrorResponse;
import ma.itroad.aace.eth.coref.model.bean.ListOfObject;
import ma.itroad.aace.eth.coref.model.bean.PortRefBean;
import ma.itroad.aace.eth.coref.model.entity.EntityRefLang;
import ma.itroad.aace.eth.coref.model.entity.PortRef;
import ma.itroad.aace.eth.coref.model.mapper.projections.PortRefLangProjection;
import ma.itroad.aace.eth.coref.service.helper.PortRefLang;

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

public interface IPortRefService extends
		IBaseService<PortRef, PortRefBean>,
		IBaseRefService,
		IRsqlService<PortRef, PortRefBean>, ImportDataService{

	Page<PortRefLang> getAll(final int page, final int size, String codeLang, String orderDirection);

	List<PortRef> saveAll(List<PortRef> portRefs);

	ErrorResponse delete(Long id);

	ErrorResponse deleteList(ListOfObject listOfObject);

	ErrorResponse deleteInternationalisation(String codeLang, Long portRefId) ;

	ByteArrayInputStream portRefToExcel(List<PortRefLang> portRefLangs);

	List<PortRefLang> excelToPortRef(InputStream is);

	ByteArrayInputStream load(String codeLang);

	Page<PortRefLang> filterByCodeOrLabel(String value, Pageable pageable, String codeLang);

	void addPort(PortRefLang portRefLang);

	PortRefLang findPort(Long id, String lang);

	void addInternationalisation(EntityRefLang entityRefLang, String lang);

    PortRefBean findById(Long id);

    Page<PortRefLangProjection> filterByCodeOrLabelProjection(String value, Pageable pageable, String lang);

	Set<ConstraintViolation<PortRefLang>> validatePortRefLang(PortRefLang portRefLang);

	ResponseEntity<?> saveFromExcelAfterValidation(MultipartFile file);
}
